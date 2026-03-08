package com.sangyoon.vehiclenote.di

import com.sangyoon.ocr.PlateRecognizer
import com.sangyoon.ocr.PlateRecognizerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OcrModule {

    @Provides
    @Singleton
    fun providePlateRecognizer(): PlateRecognizer {
        return PlateRecognizerImpl()
    }
}
