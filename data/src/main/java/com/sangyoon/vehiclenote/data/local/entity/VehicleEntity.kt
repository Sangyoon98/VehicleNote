package com.sangyoon.vehiclenote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val licencePlate: String,
    val ownerName: String,
    val department: String,
    val phoneNumber: String?,
    val carModel: String?,
    val memo: String?,
    val createdAt: Long,
    val updatedAt: Long
)
