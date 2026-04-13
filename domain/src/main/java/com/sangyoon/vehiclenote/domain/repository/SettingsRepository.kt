package com.sangyoon.vehiclenote.domain.repository

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getRetentionPeriod(): Flow<DataRetentionPeriod>
    suspend fun setRetentionPeriod(period: DataRetentionPeriod)
}
