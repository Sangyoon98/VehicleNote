package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import com.sangyoon.vehiclenote.domain.repository.EntryExitRepository
import com.sangyoon.vehiclenote.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PurgeOldRecordsUseCase @Inject constructor(
    private val entryExitRepository: EntryExitRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        val period = settingsRepository.getRetentionPeriod().first()
        val days = period.days ?: return // UNLIMITED - 삭제 안 함
        val cutoff = System.currentTimeMillis() - days * 24L * 60 * 60 * 1000
        entryExitRepository.deleteRecordsBefore(cutoff)
    }
}
