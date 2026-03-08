package com.sangyoon.ocr

import androidx.camera.core.ImageProxy

/**
 * 번호판 인식기 인터페이스.
 * :app에서 구현체를 바인딩하고, OcrEntryPoint를 통해 Composable에 주입된다.
 */
interface PlateRecognizer {
    /**
     * [imageProxy]에서 번호판을 인식한다.
     * - 인식 성공 (debounce 충족): [onResult]를 non-null String으로 호출
     * - 인식 실패 또는 cooldown 중: [onResult]를 null로 호출
     * - 반드시 내부에서 [imageProxy].close() 호출
     */
    fun recognize(imageProxy: ImageProxy, onResult: (String?) -> Unit)

    /** ML Kit 리소스 해제 */
    fun close()
}
