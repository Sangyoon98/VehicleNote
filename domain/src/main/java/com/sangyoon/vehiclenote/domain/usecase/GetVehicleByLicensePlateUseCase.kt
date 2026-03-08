package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

class GetVehicleByLicensePlateUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(plate: String): Vehicle? = repository.getByLicensePlate(plate)
}
