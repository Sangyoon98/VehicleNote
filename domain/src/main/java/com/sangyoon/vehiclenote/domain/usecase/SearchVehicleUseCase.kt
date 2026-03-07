package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    operator fun invoke(query: String): Flow<List<Vehicle>> {
        return repository.searchByLicensePlate(query)
    }
}