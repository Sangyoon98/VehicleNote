package com.sangyoon.vehiclenote.domain.repository

import com.sangyoon.vehiclenote.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

/**
 * 차량 데이터 저장소 인터페이스.
 *
 * 구현체는 data 모듈의 [VehicleRepositoryImpl]이며, Hilt로 바인딩된다.
 * 읽기 작업은 [Flow]를 반환해 Room DB 변경 시 자동으로 UI를 갱신한다.
 */
interface VehicleRepository {

    /**
     * 전체 차량 목록을 최신 등록 순으로 반환한다.
     *
     * DB가 변경될 때마다 새 목록을 emit하는 핫 스트림.
     */
    fun getAllVehicles(): Flow<List<Vehicle>>

    /**
     * 차량번호 / 차주명 / 부서 / 전화번호를 대상으로 통합 검색한다.
     *
     * @param query 검색어. 빈 문자열이면 전체 목록과 동일.
     */
    fun search(query: String): Flow<List<Vehicle>>

    /**
     * 특정 id의 차량을 단건 조회한다.
     *
     * @param id 조회할 차량의 PK.
     * @return 해당 차량 또는 존재하지 않으면 null.
     */
    suspend fun getVehicleById(id: Long): Vehicle?

    /**
     * 새 차량을 DB에 삽입한다.
     *
     * @param vehicle 저장할 차량 (id = 0으로 설정).
     * @return 삽입 후 자동 생성된 id.
     */
    suspend fun insertVehicle(vehicle: Vehicle): Long

    /**
     * 기존 차량 정보를 수정한다.
     *
     * @param vehicle 수정할 차량 (id로 기존 행을 식별).
     */
    suspend fun updateVehicle(vehicle: Vehicle)

    /**
     * 차량을 DB에서 삭제한다.
     *
     * @param vehicle 삭제할 차량.
     */
    suspend fun deleteVehicle(vehicle: Vehicle)

    /**
     * 번호판 문자열로 차량을 조회한다.
     *
     * 입출차 기록 저장 시 [vehicleId]를 연결하기 위해 사용된다.
     *
     * @param plate 조회할 차량번호.
     * @return 해당 차량 또는 미등록이면 null.
     */
    suspend fun getByLicensePlate(plate: String): Vehicle?
}
