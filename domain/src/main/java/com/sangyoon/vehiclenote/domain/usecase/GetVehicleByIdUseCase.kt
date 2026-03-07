package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

class GetVehicleByIdUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(id: Long): Vehicle? {
        return repository.getVehicleById(id)
    }
}