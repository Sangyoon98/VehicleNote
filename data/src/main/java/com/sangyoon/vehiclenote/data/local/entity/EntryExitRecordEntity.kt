package com.sangyoon.vehiclenote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entry_exit_records",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["licensePlate"]),
        Index(value = ["vehicleId"]),
        Index(value = ["timestamp"])
    ]
)
data class EntryExitRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val licensePlate: String,
    val type: String,           // RecordType.name
    val timestamp: Long,
    val vehicleId: Long?
)
