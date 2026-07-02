package com.sangyoon.vehiclenote.domain.repository

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import kotlinx.coroutines.flow.Flow

/**
 * 앱 설정 저장소 인터페이스.
 *
 * 구현체는 data 모듈의 [SettingsRepositoryImpl]이며 DataStore를 사용해 영구 저장한다.
 */
interface SettingsRepository {

    /**
     * 현재 입출차 기록 보관 기간 설정을 반환한다.
     *
     * 설정이 변경될 때마다 새 값을 emit하는 핫 스트림.
     * 기본값은 [DataRetentionPeriod.ONE_DAY].
     */
    fun getRetentionPeriod(): Flow<DataRetentionPeriod>

    /**
     * 입출차 기록 보관 기간을 변경한다.
     *
     * @param period 새로 적용할 보관 기간.
     */
    suspend fun setRetentionPeriod(period: DataRetentionPeriod)
}
