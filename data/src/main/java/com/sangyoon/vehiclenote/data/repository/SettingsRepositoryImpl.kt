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

/**
 * [SettingsRepository] 구현체.
 *
 * Jetpack DataStore를 사용해 설정값을 기기에 영구 저장한다.
 * 보관 기간은 [DataRetentionPeriod.name] 문자열로 저장되며, 기본값은 [DataRetentionPeriod.ONE_DAY].
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    /** DataStore 보관 기간 키. 값: [DataRetentionPeriod.name] 문자열. */
    private val retentionPeriodKey = stringPreferencesKey("retention_period")

    /**
     * 현재 보관 기간 설정을 반환한다.
     *
     * 저장된 값이 없으면 [DataRetentionPeriod.ONE_DAY]를 기본값으로 사용한다.
     */
    override fun getRetentionPeriod(): Flow<DataRetentionPeriod> {
        return dataStore.data.map { prefs ->
            val raw = prefs[retentionPeriodKey] ?: DataRetentionPeriod.ONE_DAY.name
            DataRetentionPeriod.valueOf(raw)
        }
    }

    /**
     * 보관 기간 설정을 변경한다.
     *
     * @param period 새로 저장할 보관 기간.
     */
    override suspend fun setRetentionPeriod(period: DataRetentionPeriod) {
        dataStore.edit { prefs ->
            prefs[retentionPeriodKey] = period.name
        }
    }
}
