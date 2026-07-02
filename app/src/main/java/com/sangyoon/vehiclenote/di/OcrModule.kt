package com.sangyoon.vehiclenote.di

import com.sangyoon.vehiclenote.ocr.PlateRecognizer
import com.sangyoon.vehiclenote.ocr.PlateRecognizerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object OcrModule {

    /**
     * 비스코프 제공 — 주입될 때마다 새 인스턴스를 생성한다.
     *
     * 인식기는 close() 후 재사용할 수 없는 리소스(ML Kit 클라이언트)이므로
     * 싱글톤으로 만들면 소유자가 close()한 뒤 앱 전체에서 인식이 영구 중단된다.
     * 소유자([com.sangyoon.vehiclenote.feature.entryexit.EntryExitViewModel])가
     * 수명에 맞춰 생성·해제한다.
     */
    @Provides
    fun providePlateRecognizer(): PlateRecognizer {
        return PlateRecognizerImpl()
    }
}
