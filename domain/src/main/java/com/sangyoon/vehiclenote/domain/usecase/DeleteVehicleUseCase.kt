package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

class DeleteVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(vehicle: Vehicle): Result<Unit> {
        return try {
            repository.deleteVehicle(vehicle)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}