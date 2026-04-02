package com.sangyoon.vehiclenote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sangyoon.vehiclenote.data.local.entity.EntryExitRecordEntity
import com.sangyoon.vehiclenote.data.local.entity.EntryExitRecordWithVehicleEntity
import com.sangyoon.vehiclenote.data.local.entity.SearchResultTuple
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryExitRecordDao {

    @Query("SELECT * FROM entry_exit_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<EntryExitRecordEntity>>

    /**
     * 번호판, 차주명, 전화번호로 검색 (LEFT JOIN).
     * Room @Relation은 SQL WHERE를 무시하므로, 플랫 튜플로 결과를 받아 직접 매핑한다.
     */
    @Query("""
        SELECT r.id, r.licensePlate, r.type, r.timestamp, r.vehicleId, v.ownerName
        FROM entry_exit_records r
        LEFT JOIN vehicles v ON r.vehicleId = v.id
        WHERE r.licensePlate LIKE '%' || :query || '%'
           OR v.ownerName LIKE '%' || :query || '%'
           OR v.phoneNumber LIKE '%' || :query || '%'
        ORDER BY r.timestamp DESC
    """)
    fun searchRecords(query: String): Flow<List<SearchResultTuple>>

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
