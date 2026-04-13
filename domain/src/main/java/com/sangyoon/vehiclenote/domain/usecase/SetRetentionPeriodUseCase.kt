package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import com.sangyoon.vehiclenote.domain.repository.SettingsRepository
import javax.inject.Inject

class SetRetentionPeriodUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(period: DataRetentionPeriod) =
        settingsRepository.setRetentionPeriod(period)
}
