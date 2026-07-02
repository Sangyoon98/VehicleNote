package com.sangyoon.vehiclenote.domain.repository

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(
    initial: DataRetentionPeriod = DataRetentionPeriod.ONE_DAY
) : SettingsRepository {
    private val _period = MutableStateFlow(initial)

    override fun getRetentionPeriod(): Flow<DataRetentionPeriod> = _period

    override suspend fun setRetentionPeriod(period: DataRetentionPeriod) {
        _period.value = period
    }
}
