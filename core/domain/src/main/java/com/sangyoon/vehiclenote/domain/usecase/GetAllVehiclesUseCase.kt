package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 전체 차량 목록을 실시간으로 조회하는 UseCase.
 *
 * [VehicleRepository.getAllVehicles]를 그대로 위임하며, DB 변경 시 자동으로 새 목록을 emit한다.
 */
class GetAllVehiclesUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    /**
     * @return 최신 등록 순으로 정렬된 차량 목록 [Flow].
     */
    operator fun invoke(): Flow<List<Vehicle>> {
        return repository.getAllVehicles()
    }
}
