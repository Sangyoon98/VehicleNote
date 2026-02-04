package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow

class GetAllVehiclesUseCase(
    private val repository: VehicleRepository
) {
    operator fun invoke(): Flow<List<Vehicle>> {
        return repository.getAllVehicles()
    }
}