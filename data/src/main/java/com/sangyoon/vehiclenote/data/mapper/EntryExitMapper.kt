package com.sangyoon.vehiclenote.data.mapper

import com.sangyoon.vehiclenote.data.local.entity.EntryExitRecordEntity
import com.sangyoon.vehiclenote.data.local.entity.EntryExitRecordWithVehicleEntity
import com.sangyoon.vehiclenote.data.local.entity.SearchResultTuple
import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.RecordType

/**
 * [EntryExitRecordEntity]를 도메인 모델 [EntryExitRecord]로 변환한다.
 *
 * 차량 정보(ownerName)는 포함되지 않는다.
 */
fun EntryExitRecordEntity.toDomain(): EntryExitRecord = EntryExitRecord(
    id = id,
    licensePlate = licensePlate,
    type = RecordType.valueOf(type),
    timestamp = timestamp,
    vehicleId = vehicleId
)

/**
 * [EntryExitRecordWithVehicleEntity]를 도메인 모델 [EntryExitRecord]로 변환한다.
 *
 * LEFT JOIN 결과이므로 [EntryExitRecordWithVehicleEntity.vehicle]이 null이면
 * [EntryExitRecord.ownerName]도 null이 된다.
 */
fun EntryExitRecordWithVehicleEntity.toDomain(): EntryExitRecord = EntryExitRecord(
    id = record.id,
    licensePlate = record.licensePlate,
    type = RecordType.valueOf(record.type),
    timestamp = record.timestamp,
    vehicleId = record.vehicleId,
    ownerName = vehicle?.ownerName
)

/**
 * 검색 쿼리 결과 [SearchResultTuple]을 도메인 모델 [EntryExitRecord]로 변환한다.
 */
fun SearchResultTuple.toDomain(): EntryExitRecord = EntryExitRecord(
    id = id,
    licensePlate = licensePlate,
    type = RecordType.valueOf(type),
    timestamp = timestamp,
    vehicleId = vehicleId,
    ownerName = ownerName
)

/**
 * 도메인 모델 [EntryExitRecord]를 [EntryExitRecordEntity]로 변환한다.
 *
 * [EntryExitRecord.ownerName]은 DB에 저장되지 않으므로 무시된다.
 */
fun EntryExitRecord.toEntity(): EntryExitRecordEntity = EntryExitRecordEntity(
    id = id,
    licensePlate = licensePlate,
    type = type.name,
    timestamp = timestamp,
    vehicleId = vehicleId
)
