package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import com.sangyoon.vehiclenote.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * 입출차 기록 보관 기간을 변경하는 UseCase.
 */
class SetRetentionPeriodUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * @param period 새로 적용할 보관 기간.
     */
    suspend operator fun invoke(period: DataRetentionPeriod) =
        settingsRepository.setRetentionPeriod(period)
}
