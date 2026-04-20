package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import javax.inject.Inject

/**
 * 새 차량을 등록하는 UseCase.
 *
 * [VehicleRepository.insertVehicle]을 호출하고 결과를 [Result]로 래핑해 반환한다.
 * 성공 시 [Result.success]에 자동 생성된 id가 담긴다.
 */
class AddVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    /**
     * @param vehicle 저장할 차량 정보 (id = 0으로 설정).
     * @return 성공 시 자동 생성된 id, 실패 시 예외를 담은 [Result].
     */
    suspend operator fun invoke(vehicle: Vehicle): Result<Long> {
        return try {
            val id = repository.insertVehicle(vehicle)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
