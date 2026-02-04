package com.sangyoon.vehiclenote.data.mapper

import com.sangyoon.vehiclenote.data.local.entity.VehicleEntity
import com.sangyoon.vehiclenote.domain.model.Vehicle

fun VehicleEntity.toDomain(): Vehicle {
    return Vehicle(
        id = id,
        licencePlate = licencePlate,
        ownerName = ownerName,
        department = department,
        phoneNumber = phoneNumber,
        carModel = carModel,
        memo = memo,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Vehicle.toEntity(): VehicleEntity {
    return VehicleEntity(
        id = id,
        licencePlate = licencePlate,
        ownerName = ownerName,
        department = department,
        phoneNumber = phoneNumber,
        carModel = carModel,
        memo = memo,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}