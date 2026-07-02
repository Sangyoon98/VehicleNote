package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 차량 목록을 일괄 등록하는 UseCase (CSV 가져오기 등).
 *
 * 등록 규칙:
 * - 이미 같은 번호판이 등록되어 있으면 건너뛴다 (기존 데이터 보존).
 * - 나머지는 새 차량으로 등록한다.
 */
class ImportVehiclesUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    /**
     * 가져오기 결과.
     *
     * @property addedCount 새로 등록된 차량 수.
     * @property skippedByDuplicate 번호판 중복으로 건너뛴 차량 수.
     */
    data class ImportResult(
        val addedCount: Int,
        val skippedByDuplicate: Int
    )

    /**
     * @param vehicles 등록할 차량 목록 (id = 0으로 설정).
     * @return 등록/건너뜀 건수를 담은 [ImportResult].
     */
    suspend operator fun invoke(vehicles: List<Vehicle>): ImportResult {
        var added = 0
        var skippedDuplicate = 0
        for (vehicle in vehicles) {
            if (repository.getByLicensePlate(vehicle.licensePlate) != null) {
                skippedDuplicate++
            } else {
                repository.insertVehicle(vehicle)
                added++
            }
        }
        return ImportResult(added, skippedDuplicate)
    }
}
