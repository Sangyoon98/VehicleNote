package com.sangyoon.vehiclenote.di

import com.sangyoon.ocr.PlateRecognizer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Composable에서 PlateRecognizer를 주입받기 위한 Hilt EntryPoint.
 *
 * 사용법:
 * ```kotlin
 * val plateRecognizer = remember {
 *     EntryPointAccessors.fromApplication(
 *         context.applicationContext,
 *         OcrEntryPoint::class.java
 *     ).plateRecognizer()
 * }
 * ```
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface OcrEntryPoint {
    fun plateRecognizer(): PlateRecognizer
}
