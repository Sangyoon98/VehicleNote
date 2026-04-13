package com.sangyoon.vehiclenote.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import com.sangyoon.vehiclenote.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private val retentionPeriodKey = stringPreferencesKey("retention_period")

    override fun getRetentionPeriod(): Flow<DataRetentionPeriod> {
        return dataStore.data.map { prefs ->
            val raw = prefs[retentionPeriodKey] ?: DataRetentionPeriod.ONE_DAY.name
            DataRetentionPeriod.valueOf(raw)
        }
    }

    override suspend fun setRetentionPeriod(period: DataRetentionPeriod) {
        dataStore.edit { prefs ->
            prefs[retentionPeriodKey] = period.name
        }
    }
}
