package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

class AddVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(vehicle: Vehicle): Result<Long> {
        return try {
            val id = repository.insertVehicle(vehicle)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}