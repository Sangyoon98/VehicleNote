package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 차량 정보를 수정하는 UseCase.
 *
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
