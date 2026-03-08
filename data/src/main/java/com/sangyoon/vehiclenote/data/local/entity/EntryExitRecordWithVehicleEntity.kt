package com.sangyoon.vehiclenote.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * LEFT JOIN 결과용 데이터 클래스 (Room @Relation).
 * entry_exit_records LEFT JOIN vehicles ON vehicleId = vehicles.id
 */
data class EntryExitRecordWithVehicleEntity(
    @Embedded val record: EntryExitRecordEntity,
    @Relation(
        parentColumn = "vehicleId",
        entityColumn = "id"
    )
    val vehicle: VehicleEntity?
)
