package com.sangyoon.vehiclenote.data.repository

import com.sangyoon.vehiclenote.data.local.dao.VehicleDao
import com.sangyoon.vehiclenote.data.mapper.toDomain
import com.sangyoon.vehiclenote.data.mapper.toEntity
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [VehicleRepository] 구현체.
 *
 * [VehicleDao]를 통해 Room DB에 접근하며, 엔티티 ↔ 도메인 변환은 mapper 함수에 위임한다.
 * Hilt `@Singleton`으로 등록되어 앱 생명주기 동안 단일 인스턴스를 유지한다.
 */
@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao
) : VehicleRepository {

    /** 전체 차량 목록을 최신 등록 순으로 반환한다. DB 변경 시 자동 갱신. */
    override fun getAllVehicles(): Flow<List<Vehicle>> {
        return vehicleDao.getAllVehicles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /** 차량번호 / 차주명 / 부서 / 전화번호 통합 검색. */
    override fun search(query: String): Flow<List<Vehicle>> {
        return vehicleDao.search(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /** 특정 id 차량 단건 조회. 없으면 null. */
    override suspend fun getVehicleById(id: Long): Vehicle? {
        return vehicleDao.getVehicleById(id)?.toDomain()
    }

    /** 새 차량을 삽입하고 자동 생성된 id를 반환한다. */
    override suspend fun insertVehicle(vehicle: Vehicle): Long {
        return vehicleDao.insertVehicle(vehicle.toEntity())
    }

    /** 기존 차량 정보를 수정한다. */
    override suspend fun updateVehicle(vehicle: Vehicle) {
        vehicleDao.updateVehicle(vehicle.toEntity())
    }

    /** 차량을 삭제한다. */
    override suspend fun deleteVehicle(vehicle: Vehicle) {
        vehicleDao.deleteVehicle(vehicle.toEntity())
    }

    /** 번호판으로 차량을 조회한다. 미등록이면 null. */
    override suspend fun getByLicensePlate(plate: String): Vehicle? {
        return vehicleDao.getByLicensePlate(plate)?.toDomain()
    }
}
