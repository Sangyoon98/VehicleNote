package com.sangyoon.vehiclenote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sangyoon.vehiclenote.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY createdAt DESC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("""
        SELECT * FROM vehicles
        WHERE licensePlate LIKE '%' || :query || '%'
           OR ownerName   LIKE '%' || :query || '%'
           OR department  LIKE '%' || :query || '%'
           OR phoneNumber LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun search(query: String): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: Long): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    @Query("SELECT * FROM vehicles WHERE licensePlate = :plate LIMIT 1")
    suspend fun getByLicensePlate(plate: String): VehicleEntity?
}