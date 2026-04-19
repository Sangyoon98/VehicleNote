package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import com.sangyoon.vehiclenote.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 현재 입출차 기록 보관 기간 설정을 조회하는 UseCase.
 *
 * 설정이 변경될 때마다 새 값을 emit하는 핫 스트림을 반환한다.
 */
class GetRetentionPeriodUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * @return 현재 보관 기간 [Flow]. 기본값은 [DataRetentionPeriod.ONE_DAY].
     */
    operator fun invoke(): Flow<DataRetentionPeriod> = settingsRepository.getRetentionPeriod()
}
