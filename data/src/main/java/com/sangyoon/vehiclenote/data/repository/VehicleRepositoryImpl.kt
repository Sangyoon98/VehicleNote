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

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao
) : VehicleRepository {
    override fun getAllVehicles(): Flow<List<Vehicle>> {
        return vehicleDao.getAllVehicles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchByLicensePlate(query: String): Flow<List<Vehicle>> {
        return vehicleDao.searchByLicensePlate(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getVehicleById(id: Long): Vehicle? {
        return vehicleDao.getVehicleById(id)?.toDomain()
    }

    override suspend fun insertVehicle(vehicle: Vehicle): Long {
        return vehicleDao.insertVehicle(vehicle.toEntity())
    }

    override suspend fun updateVehicle(vehicle: Vehicle) {
        vehicleDao.updateVehicle(vehicle.toEntity())
    }

    override suspend fun deleteVehicle(vehicle: Vehicle) {
        vehicleDao.deleteVehicle(vehicle.toEntity())
    }
}