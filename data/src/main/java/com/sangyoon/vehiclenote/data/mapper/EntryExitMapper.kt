package com.sangyoon.vehiclenote.data.mapper

import com.sangyoon.vehiclenote.data.local.entity.EntryExitRecordEntity
import com.sangyoon.vehiclenote.data.local.entity.EntryExitRecordWithVehicleEntity
import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.RecordType

fun EntryExitRecordEntity.toDomain(): EntryExitRecord = EntryExitRecord(
    id = id,
    licensePlate = licensePlate,
    type = RecordType.valueOf(type),
    timestamp = timestamp,
    vehicleId = vehicleId
)

fun EntryExitRecordWithVehicleEntity.toDomain(): EntryExitRecord = EntryExitRecord(
    id = record.id,
    licensePlate = record.licensePlate,
    type = RecordType.valueOf(record.type),
    timestamp = record.timestamp,
    vehicleId = record.vehicleId,
    ownerName = vehicle?.ownerName
)

fun EntryExitRecord.toEntity(): EntryExitRecordEntity = EntryExitRecordEntity(
    id = id,
    licensePlate = licensePlate,
    type = type.name,
    timestamp = timestamp,
    vehicleId = vehicleId
)
