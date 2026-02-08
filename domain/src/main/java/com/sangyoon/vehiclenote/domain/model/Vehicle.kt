package com.sangyoon.vehiclenote.domain.model

data class Vehicle(
    val id: Long = 0,
    val licensePlate: String,
    val ownerName: String,
    val department: String,
    val phoneNumber: String? = null,
    val carModel: String? = null,
    val memo: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
