package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 특정 id의 차량을 단건 조회하는 UseCase.
 */
class GetVehicleByIdUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    /**
     * @param id 조회할 차량의 PK.
     * @return 해당 차량, 존재하지 않으면 null.
     */
    suspend operator fun invoke(id: Long): Vehicle? {
        return repository.getVehicleById(id)
    }
}
