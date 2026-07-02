package com.sangyoon.vehiclenote.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsLogger @Inject constructor(
    @ApplicationContext context: Context
) {
    private val fa = FirebaseAnalytics.getInstance(context)

    fun vehicleRegistered(licensePlate: String) =
        fa.logEvent("vehicle_registered", bundle("license_plate" to licensePlate))

    fun vehicleUpdated(licensePlate: String) =
        fa.logEvent("vehicle_updated", bundle("license_plate" to licensePlate))

    fun vehicleDeleted(licensePlate: String) =
        fa.logEvent("vehicle_deleted", bundle("license_plate" to licensePlate))

    fun entryExitRecorded(licensePlate: String, type: String) =
        fa.logEvent("entry_exit_recorded", bundle(
            "license_plate" to licensePlate,
            "type" to type,
        ))

    fun csvExported(vehicleCount: Int) =
        fa.logEvent("csv_exported", bundle("vehicle_count" to vehicleCount.toLong()))

    fun csvImported(addedCount: Int, skippedCount: Int) =
        fa.logEvent("csv_imported", bundle(
            "added_count" to addedCount.toLong(),
            "skipped_count" to skippedCount.toLong(),
        ))

    fun retentionPeriodChanged(period: String) =
        fa.logEvent("retention_period_changed", bundle("period" to period))

    private fun bundle(vararg pairs: Pair<String, Any>): Bundle = Bundle().apply {
        pairs.forEach { (key, value) ->
            when (value) {
                is String -> putString(key, value)
                is Long   -> putLong(key, value)
                is Int    -> putInt(key, value)
                else      -> putString(key, value.toString())
            }
        }
    }
}
