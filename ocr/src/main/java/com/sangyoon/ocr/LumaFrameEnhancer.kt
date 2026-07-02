package com.sangyoon.ocr

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import com.google.mlkit.vision.common.InputImage

/**
 * 저조도·저대비 프레임의 Y(휘도) 플레인을 대비 보정해 OCR 입력으로 변환한다.
 *
 * - 프레임 휘도 히스토그램을 서브샘플링으로 계산해, 어둡거나 대비가 좁은 프레임만
 *   선별적으로 보정한다 (밝은 주간 프레임은 원본 그대로 사용 — 오버헤드 없음).
 * - 5~95 퍼센타일을 0~255로 선형 스트레치해 글자·배경 대비를 확보한다.
 * - 색상(UV) 정보를 버리고 휘도만 사용하므로 전기차(파랑)·영업용(노랑) 등
 *   유색 번호판도 배경색과 무관하게 처리된다.
 * - 단일 분석 스레드에서만 호출되는 전제로 내부 버퍼를 재사용한다.
 */
internal class LumaFrameEnhancer {

    private var pixelBuffer = IntArray(0)
    private var reusableBitmap: Bitmap? = null
    private val histogram = IntArray(256)
    private val lut = IntArray(256)

    /**
     * 보정이 필요한 프레임이면 대비 보정된 그레이스케일 [InputImage]를 반환한다.
     * 보정이 필요 없거나 지원하지 않는 포맷이면 null (호출부는 원본 프레임 사용).
     */
    fun enhanceIfNeeded(image: Image, rotationDegrees: Int): InputImage? {
        if (image.format != ImageFormat.YUV_420_888) return null

        val plane = image.planes[0]
        val buffer = plane.buffer
        val width = image.width
        val height = image.height
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride

        histogram.fill(0)
        var sampleCount = 0
        var row = 0
        while (row < height) {
            val rowOffset = row * rowStride
            var col = 0
            while (col < width) {
                val luma = buffer.get(rowOffset + col * pixelStride).toInt() and 0xFF
                histogram[luma]++
                sampleCount++
                col += SAMPLE_STEP
            }
            row += SAMPLE_STEP
        }
        if (sampleCount == 0) return null

        var sum = 0L
        for (v in 0..255) sum += v.toLong() * histogram[v]
        val mean = (sum / sampleCount).toInt()
        val p5 = percentile(sampleCount, 5)
        val p95 = percentile(sampleCount, 95)
        val range = p95 - p5

        if (mean >= DARK_MEAN_THRESHOLD && range >= LOW_CONTRAST_RANGE) return null

        val stretchRange = range.coerceAtLeast(MIN_STRETCH_RANGE)
        for (v in 0..255) {
            lut[v] = ((v - p5) * 255 / stretchRange).coerceIn(0, 255)
        }

        val pixels = obtainPixelBuffer(width * height)
        var index = 0
        for (y in 0 until height) {
            val rowOffset = y * rowStride
            for (x in 0 until width) {
                val luma = buffer.get(rowOffset + x * pixelStride).toInt() and 0xFF
                val stretched = lut[luma]
                pixels[index++] = 0xFF shl 24 or (stretched shl 16) or (stretched shl 8) or stretched
            }
        }

        val bitmap = obtainBitmap(width, height)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return InputImage.fromBitmap(bitmap, rotationDegrees)
    }

    private fun percentile(sampleCount: Int, percent: Int): Int {
        val target = sampleCount.toLong() * percent / 100
        var cumulative = 0L
        for (v in 0..255) {
            cumulative += histogram[v]
            if (cumulative >= target) return v
        }
        return 255
    }

    private fun obtainPixelBuffer(size: Int): IntArray {
        if (pixelBuffer.size < size) pixelBuffer = IntArray(size)
        return pixelBuffer
    }

    private fun obtainBitmap(width: Int, height: Int): Bitmap {
        val current = reusableBitmap
        if (current != null && current.width == width && current.height == height) return current
        current?.recycle()
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            .also { reusableBitmap = it }
    }

    private companion object {
        /** 휘도 통계 서브샘플링 간격 (양방향) — 전체 픽셀의 1/64만 검사 */
        const val SAMPLE_STEP = 8

        /** 프레임 평균 휘도가 이보다 낮으면 저조도로 판단 */
        const val DARK_MEAN_THRESHOLD = 80

        /** p95-p5 휘도 범위가 이보다 좁으면 저대비로 판단 */
        const val LOW_CONTRAST_RANGE = 90

        /** 스트레치 분모 하한 (노이즈 과증폭 방지) */
        const val MIN_STRETCH_RANGE = 32
    }
}
