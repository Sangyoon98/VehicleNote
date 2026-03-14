package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.RecordType
import com.sangyoon.vehiclenote.domain.repository.EntryExitRepository
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 입출차 기록 저장 UseCase.
 * - 마지막 상태 토글: 마지막=ENTRY면 EXIT, 마지막=EXIT 또는 없음이면 ENTRY
 * - 등록된 차량이 있으면 vehicleId 연결
 */
class RecordEntryExitUseCase @Inject constructor(
    private val entryExitRepository: EntryExitRepository,
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(licensePlate: String): EntryExitRecord {
        val lastRecord = entryExitRepository.getLastRecordByPlate(licensePlate)
        val newType =
            if (lastRecord?.type == RecordType.ENTRY) RecordType.EXIT else RecordType.ENTRY
        val vehicle = vehicleRepository.getByLicensePlate(licensePlate)

        val record = EntryExitRecord(
            licensePlate = licensePlate,
            type = newType,
            vehicleId = vehicle?.id
        )
        val id = entryExitRepository.insertRecord(record)
        return record.copy(id = id, ownerName = vehicle?.ownerName)
    }
}
