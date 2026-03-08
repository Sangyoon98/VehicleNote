package com.sangyoon.vehiclenote.domain.model

data class EntryExitRecord(
    val id: Long = 0,
    val licensePlate: String,
    val type: RecordType,
    val timestamp: Long = System.currentTimeMillis(),
    val vehicleId: Long? = null,
    // JOIN 결과에서 채워지는 필드 (optional)
    val ownerName: String? = null
)
