package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 차량을 삭제하는 UseCase.
 *
 * [VehicleRepository.deleteVehicle]을 호출하고 결과를 [Result]로 래핑한다.
 */
class DeleteVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    /**
     * @param vehicle 삭제할 차량.
     * @return 성공 시 [Result.success], 실패 시 예외를 담은 [Result].
     */
    suspend operator fun invoke(vehicle: Vehicle): Result<Unit> {
        return try {
            repository.deleteVehicle(vehicle)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
