package com.sangyoon.vehiclenote

import android.app.Application
import com.sangyoon.vehiclenote.domain.usecase.PurgeOldRecordsUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VehicleNoteApp : Application() {

    @Inject
    lateinit var purgeOldRecordsUseCase: PurgeOldRecordsUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // 보관 기간이 지난 입출차 기록 정리 — 프로세스 시작 시 1회
        // (Activity에 두면 화면 회전 등 재생성마다 반복 실행됨)
        applicationScope.launch { purgeOldRecordsUseCase() }
    }
}
