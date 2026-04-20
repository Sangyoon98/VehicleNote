package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 차량번호 / 차주명 / 부서 / 전화번호를 통합 검색하는 UseCase.
 *
 * 검색어가 변경될 때마다 새 결과를 emit하는 핫 스트림을 반환한다.
 */
class SearchVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    /**
     * @param query 검색어. 빈 문자열이면 전체 목록과 동일.
     * @return 검색 결과 차량 목록 [Flow].
     */
    operator fun invoke(query: String): Flow<List<Vehicle>> {
        return repository.search(query)
    }
}
