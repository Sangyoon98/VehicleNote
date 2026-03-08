package com.sangyoon.vehiclenote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sangyoon.vehiclenote.data.local.entity.EntryExitRecordEntity
import com.sangyoon.vehiclenote.data.local.entity.EntryExitRecordWithVehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryExitRecordDao {

    @Query("SELECT * FROM entry_exit_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<EntryExitRecordEntity>>

    /**
     * 번호판 또는 차주명으로 검색 (LEFT JOIN).
     * Room @Transaction + @Relation 대신 raw SQL 쿼리로 LEFT JOIN 처리.
     * 단, 결과를 EntryExitRecordWithVehicleEntity로 받기 위해 @Transaction 사용.
     */
    @Transaction
    @Query("""
        SELECT r.* FROM entry_exit_records r
        LEFT JOIN vehicles v ON r.vehicleId = v.id
        WHERE r.licensePlate LIKE '%' || :query || '%'
           OR v.ownerName LIKE '%' || :query || '%'
        ORDER BY r.timestamp DESC
    """)
    fun searchRecords(query: String): Flow<List<EntryExitRecordWithVehicleEntity>>

    @Query("SELECT * FROM entry_exit_records WHERE licensePlate = :plate ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastByPlate(plate: String): EntryExitRecordEntity?

    @Query("SELECT * FROM entry_exit_records WHERE id = :id")
    suspend fun getById(id: Long): EntryExitRecordEntity?

    @Transaction
    @Query("SELECT * FROM entry_exit_records WHERE id = :id")
    suspend fun getByIdWithVehicle(id: Long): EntryExitRecordWithVehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: EntryExitRecordEntity): Long
}
