package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 차량 정보를 수정하는 UseCase.
 *
 * 차량번호와 차주명은 필수 — 비어 있으면 [IllegalArgumentException] 실패를 반환한다.
 * 저장 직전에 [Vehicle.updatedAt]을 현재 시각으로 갱신한다.
 */
class UpdateVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    /**
     * @param vehicle 수정할 차량 정보. [Vehicle.updatedAt]은 자동으로 갱신됨.
     * @return 성공 시 [Result.success], 실패 시 예외를 담은 [Result].
     */
    suspend operator fun invoke(vehicle: Vehicle): Result<Unit> {
        if (vehicle.licensePlate.isBlank()) {
            return Result.failure(IllegalArgumentException("차량번호는 필수입니다"))
        }
        if (vehicle.ownerName.isBlank()) {
            return Result.failure(IllegalArgumentException("차주명은 필수입니다"))
        }
        return try {
            val updatedVehicle = vehicle.copy(
                updatedAt = System.currentTimeMillis()
            )
            repository.updateVehicle(updatedVehicle)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
