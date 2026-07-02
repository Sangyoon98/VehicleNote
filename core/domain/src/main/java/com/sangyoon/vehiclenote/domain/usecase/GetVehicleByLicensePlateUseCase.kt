package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 번호판 문자열로 차량을 조회하는 UseCase.
 *
 * 주로 입출차 기록 저장 시 [vehicleId]를 연결하기 위해 사용된다.
 */
class GetVehicleByLicensePlateUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    /**
     * @param plate 조회할 차량번호.
     * @return 해당 차량, 미등록이면 null.
     */
    suspend operator fun invoke(plate: String): Vehicle? = repository.getByLicensePlate(plate)
}
