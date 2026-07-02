package com.sangyoon.vehiclenote.ocr

import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

/**
 * Google ML Kit Korean OCR 기반 번호판 인식기.
 *
 * 인식 파이프라인:
 * 1. [LumaFrameEnhancer]가 저조도·저대비 프레임을 대비 보정 (야간·유색 번호판 대응)
 * 2. [KoreanPlateFilter]가 프레임의 모든 번호판 후보를 신뢰도 순으로 추출
 * 3. [PlateTracker]가 번호판별 독립 추적으로 확정 (프레임 누락 허용, 다중 번호판 동시 추적)
 *    — 확정된 번호판은 화면에서 사라질 때까지 쿨다운이 연장되어 반복 확정되지 않는다.
 */
class PlateRecognizerImpl : PlateRecognizer {

    private val recognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    )

    private val tracker = PlateTracker()
    private val frameEnhancer = LumaFrameEnhancer()

    @OptIn(ExperimentalGetImage::class)
    override fun recognize(imageProxy: ImageProxy, onResult: (plate: String?, boundingBox: Rect?) -> Unit) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            onResult(null, null)
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = frameEnhancer.enhanceIfNeeded(mediaImage, rotation)
            ?: InputImage.fromMediaImage(mediaImage, rotation)

        // ML Kit 바운딩 박스는 회전 보정된 좌표계 기준 — 중앙 근접도 계산용 프레임 크기도 맞춘다
        val rotated = rotation == 90 || rotation == 270
        val frameWidth = if (rotated) mediaImage.height else mediaImage.width
        val frameHeight = if (rotated) mediaImage.width else mediaImage.height

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val now = System.currentTimeMillis()
                val result = KoreanPlateFilter.findPlateCandidates(
                    visionText = visionText,
                    ignoredPlates = tracker.activeCooldownPlates(now),
                    imageWidth = frameWidth,
                    imageHeight = frameHeight,
                )
                // 쿨다운 중인 번호판이 아직 화면에 있으면 쿨다운 연장 (같은 차량 반복 확정 방지)
                tracker.refreshCooldowns(result.matchedIgnored, now)

                val tracked = result.candidates.take(MAX_TRACKED_PER_FRAME)
                val confirmed = tracker.onFrame(tracked.map { it.plate }, now)
                if (confirmed != null) {
                    val boundingBox = tracked.firstOrNull {
                        KoreanPlateFilter.isSimilarPlate(it.plate, confirmed)
                    }?.boundingBox
                    onResult(confirmed, boundingBox)
                } else {
                    onResult(null, null)
                }
            }
            .addOnFailureListener {
                onResult(null, null)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    override fun close() {
        tracker.reset()
        recognizer.close()
    }

    private companion object {
        /** 프레임당 추적할 최대 후보 수 — 다중 번호판 프레임 대응 */
        const val MAX_TRACKED_PER_FRAME = 3
    }
}
