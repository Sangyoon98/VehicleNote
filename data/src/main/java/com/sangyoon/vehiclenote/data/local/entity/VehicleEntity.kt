package com.sangyoon.vehiclenote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val licensePlate: String,
    val ownerName: String,
    val department: String,       // DB에서 NOT NULL 유지 (v1 호환). 빈 문자열이면 "미지정" 처리
    val phoneNumber: String?,
    val carModel: String?,
    val memo: String?,
    val photoUri: String?,
    val customFields: String,
    val createdAt: Long,
    val updatedAt: Long
)
