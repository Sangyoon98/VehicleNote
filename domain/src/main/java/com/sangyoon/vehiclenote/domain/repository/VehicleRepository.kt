package com.sangyoon.vehiclenote.domain.repository

import com.sangyoon.vehiclenote.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    // 전체 차량 목록 조회 (Flow로 실시간 업데이트)
    fun getAllVehicles(): Flow<List<Vehicle>>

    // 차량 번호로 검색
    fun searchByLicensePlate(query: String): Flow<List<Vehicle>>

    // 특정 차량 조회
    suspend fun getVehicleById(id: Long): Vehicle?

    // 차량 추가
    suspend fun insertVehicle(vehicle: Vehicle): Long

    // 치량 수정
    suspend fun updateVehicle(vehicle: Vehicle)

    // 차량 삭제
    suspend fun deleteVehicle(vehicle: Vehicle)

    // 번호판으로 차량 조회 (입출차 기록 시 vehicleId 연결용)
    suspend fun getByLicensePlate(plate: String): Vehicle?
}