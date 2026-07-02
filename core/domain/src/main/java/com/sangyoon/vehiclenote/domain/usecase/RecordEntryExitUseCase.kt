package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.RecordType
import com.sangyoon.vehiclenote.domain.repository.EntryExitRepository
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 입출차 기록을 저장하는 UseCase.
 *
 * 동작 규칙:
 * - 마지막 상태 토글: 마지막 기록이 [RecordType.ENTRY]이면 → [RecordType.EXIT],
 *   마지막 기록이 [RecordType.EXIT]이거나 기록 없음이면 → [RecordType.ENTRY].
 * - 등록된 차량이 있으면 [EntryExitRecord.vehicleId]를 자동으로 연결한다.
 */
class RecordEntryExitUseCase @Inject constructor(
    private val entryExitRepository: EntryExitRepository,
    private val vehicleRepository: VehicleRepository
) {
    /**
     * 번호판에 대한 입출차 기록을 자동 판별해 저장한다.
     *
     * @param licensePlate 기록할 차량번호.
     * @return 저장된 기록 (id와 ownerName이 채워진 상태).
     */
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
