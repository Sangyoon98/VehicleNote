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
 * - 동일 번호판이 [REQUIRED_CONSECUTIVE]프레임 연속 인식되어야 결과 전달
 * - 마지막 인식 성공 후 [COOLDOWN_MS]동안 재인식 억제
 */
class PlateRecognizerImpl : PlateRecognizer {

    private val recognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    )

    private var lastRecognizedPlate: String? = null
    private var consecutiveCount = 0
    private var lastDetectionTime = 0L

    private val REQUIRED_CONSECUTIVE = 2
    private val COOLDOWN_MS = 3000L

    @OptIn(ExperimentalGetImage::class)
    override fun recognize(imageProxy: ImageProxy, onResult: (plate: String?, boundingBox: Rect?) -> Unit) {
        val now = System.currentTimeMillis()

        // Cooldown 중이면 프레임 스킵
        if (now - lastDetectionTime < COOLDOWN_MS) {
            imageProxy.close()
            onResult(null, null)
            return
        }

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
                val detection = KoreanPlateFilter.findPlateBounds(visionText)
                val plate = detection?.first
                val boundingBox = detection?.second

                when {
                    plate == null -> {
                        lastRecognizedPlate = null
                        consecutiveCount = 0
                        onResult(null, null)
                    }

                    plate == lastRecognizedPlate -> {
                        consecutiveCount++
                        if (consecutiveCount >= REQUIRED_CONSECUTIVE) {
                            consecutiveCount = 0
                            lastDetectionTime = now
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
}
