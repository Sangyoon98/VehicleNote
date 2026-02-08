package com.sangyoon.vehiclenote.domain.repository

import com.sangyoon.vehiclenote.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeVehicleRepository : VehicleRepository {
    private val vehicles = mutableListOf<Vehicle>()
    private var currentId = 1L

    override fun getAllVehicles(): Flow<List<Vehicle>> = flow {
        emit(vehicles.toList())
    }

    override fun searchByLicensePlate(query: String): Flow<List<Vehicle>> = flow {
        emit(vehicles.filter { it.licensePlate.contains(query, ignoreCase = true) })
    }

    override suspend fun getVehicleById(id: Long): Vehicle? {
        return vehicles.find { it.id == id }
    }

    override suspend fun insertVehicle(vehicle: Vehicle): Long {
        val newVehicle = vehicle.copy(id = currentId++)
        vehicles.add(newVehicle)
        return newVehicle.id
    }

    override suspend fun updateVehicle(vehicle: Vehicle) {
        val index = vehicles.indexOfFirst { it.id == vehicle.id }
        if (index != -1) {
            vehicles[index] = vehicle
        }
    }

    override suspend fun deleteVehicle(vehicle: Vehicle) {
        vehicles.removeIf { it.id == vehicle.id }
    }

    fun clear() {
        vehicles.clear()
        currentId = 1L
    }
}