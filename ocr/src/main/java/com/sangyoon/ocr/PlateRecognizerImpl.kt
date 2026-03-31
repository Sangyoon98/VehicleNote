package com.sangyoon.ocr

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
 * Debounce 전략:
 * - 동일(또는 유사) 번호판이 [REQUIRED_CONSECUTIVE]프레임 연속 인식되어야 결과 전달
 * - 마지막 인식 성공 후 동일 번호판은 [COOLDOWN_MS]동안 재인식 억제 (다른 번호판은 즉시 인식 가능)
 */
class PlateRecognizerImpl : PlateRecognizer {

    private val recognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    )

    private var lastRecognizedPlate: String? = null
    private var consecutiveCount = 0

    /** 번호판별 마지막 인식 시각 (동일 번호판만 쿨다운 적용) */
    private val cooldownMap = mutableMapOf<String, Long>()

    private val REQUIRED_CONSECUTIVE = 2
    private val COOLDOWN_MS = 2000L
    private val COOLDOWN_MAP_MAX_SIZE = 20

    @OptIn(ExperimentalGetImage::class)
    override fun recognize(imageProxy: ImageProxy, onResult: (plate: String?, boundingBox: Rect?) -> Unit) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            onResult(null, null)
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val now = System.currentTimeMillis()
                val detection = KoreanPlateFilter.findPlateBounds(visionText)
                val plate = detection?.first
                val boundingBox = detection?.second

                when {
                    plate == null -> {
                        lastRecognizedPlate = null
                        consecutiveCount = 0
                        onResult(null, null)
                    }

                    isInCooldown(plate, now) -> {
                        // 동일 번호판 쿨다운 중 — 스킵
                        onResult(null, null)
                    }

                    isSameAsLast(plate) -> {
                        consecutiveCount++
                        if (consecutiveCount >= REQUIRED_CONSECUTIVE) {
                            consecutiveCount = 0
                            addCooldown(plate, now)
                            onResult(plate, boundingBox)
                        } else {
                            onResult(null, null)
                        }
                    }

                    else -> {
                        lastRecognizedPlate = plate
                        consecutiveCount = 1
                        onResult(null, null)
                    }
                }
            }
            .addOnFailureListener {
                lastRecognizedPlate = null
                consecutiveCount = 0
                onResult(null, null)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    override fun close() {
        recognizer.close()
    }

    /**
     * 현재 프레임의 번호판이 직전 프레임과 동일(또는 유사)한지 판별.
     * OCR 1글자 오차를 허용하여 인식률을 높인다.
     */
    private fun isSameAsLast(plate: String): Boolean {
        val last = lastRecognizedPlate ?: return false
        return KoreanPlateFilter.isSimilarPlate(last, plate)
    }

    /**
     * 해당 번호판이 쿨다운 중인지 확인.
     * 유사 번호판(1글자 오차)도 쿨다운에 해당한다.
     */
    private fun isInCooldown(plate: String, now: Long): Boolean {
        return cooldownMap.any { (cooledPlate, time) ->
            now - time < COOLDOWN_MS && KoreanPlateFilter.isSimilarPlate(cooledPlate, plate)
        }
    }

    private fun addCooldown(plate: String, now: Long) {
        cooldownMap[plate] = now
        if (cooldownMap.size > COOLDOWN_MAP_MAX_SIZE) {
            val cutoff = now - COOLDOWN_MS
            cooldownMap.entries.removeAll { it.value < cutoff }
        }
    }
}
