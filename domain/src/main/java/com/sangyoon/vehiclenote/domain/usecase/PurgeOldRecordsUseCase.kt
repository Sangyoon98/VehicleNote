package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import com.sangyoon.vehiclenote.domain.repository.EntryExitRepository
import com.sangyoon.vehiclenote.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 보관 기간이 지난 입출차 기록을 일괄 삭제하는 UseCase.
 *
 * [SettingsRepository]에서 현재 보관 기간을 읽고,
 * [DataRetentionPeriod.UNLIMITED]가 아닌 경우에만 기간 초과 기록을 삭제한다.
 * 앱 시작 시 [MainActivity.onCreate]에서 한 번 호출된다.
 */
class PurgeOldRecordsUseCase @Inject constructor(
    private val entryExitRepository: EntryExitRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * 보관 기간 설정을 읽어 초과 기록을 삭제한다.
     * [DataRetentionPeriod.UNLIMITED]이면 아무 작업도 수행하지 않는다.
     */
    suspend operator fun invoke() {
        val period = settingsRepository.getRetentionPeriod().first()
        val days = period.days ?: return // UNLIMITED - 삭제 안 함
        val cutoff = System.currentTimeMillis() - days * 24L * 60 * 60 * 1000
        entryExitRepository.deleteRecordsBefore(cutoff)
    }
}
