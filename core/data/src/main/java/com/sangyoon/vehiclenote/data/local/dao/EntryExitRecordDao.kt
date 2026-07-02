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

/**
 * 입출차 기록 테이블(`entry_exit_records`) Room DAO.
 */
@Dao
interface EntryExitRecordDao {

    /**
     * 전체 입출차 기록을 최신 순으로 반환한다.
     *
     * DB 변경 시 자동으로 새 목록을 emit하는 핫 스트림.
     */
    @Query("SELECT * FROM entry_exit_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<EntryExitRecordEntity>>

    /**
     * 번호판, 차주명, 전화번호로 입출차 기록을 검색한다 (LEFT JOIN).
     *
     * Room `@Relation`은 SQL WHERE를 무시하므로, 플랫 [SearchResultTuple]로 결과를 받아 직접 매핑한다.
     *
     * @param query 검색어. SQL LIKE 패턴으로 변환됨.
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

    /**
     * 특정 번호판의 가장 최근 기록을 조회한다.
     *
     * 다음 입출차 유형(토글)을 결정하기 위해 사용된다.
     *
     * @param plate 조회할 차량번호.
     * @return 가장 최근 기록, 없으면 null.
     */
    @Query("SELECT * FROM entry_exit_records WHERE licensePlate = :plate ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastByPlate(plate: String): EntryExitRecordEntity?

    /**
     * 특정 id의 기록을 단건 조회한다 (차량 정보 미포함).
     *
     * @param id 조회할 기록의 PK.
     * @return 해당 기록, 없으면 null.
     */
    @Query("SELECT * FROM entry_exit_records WHERE id = :id")
    suspend fun getById(id: Long): EntryExitRecordEntity?

    /**
     * 특정 id의 기록을 차량 정보와 함께 조회한다 (LEFT JOIN 포함).
     *
     * `@Transaction`으로 원자적으로 실행되며, [EntryExitRecordWithVehicleEntity.vehicle]에
     * 연결된 차량 정보가 채워진다.
     *
     * @param id 조회할 기록의 PK.
     * @return 기록+차량 결합 엔티티, 없으면 null.
     */
    @Transaction
    @Query("SELECT * FROM entry_exit_records WHERE id = :id")
    suspend fun getByIdWithVehicle(id: Long): EntryExitRecordWithVehicleEntity?

    /**
     * 입출차 기록을 삽입한다.
     *
     * @param record 삽입할 기록 엔티티.
     * @return 자동 생성된 id.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: EntryExitRecordEntity): Long

    /**
     * 지정 시각 이전의 기록을 일괄 삭제한다.
     *
     * @param beforeTimestamp 이 시각(epoch ms) 이전 기록을 모두 삭제.
     */
    @Query("DELETE FROM entry_exit_records WHERE timestamp < :beforeTimestamp")
    suspend fun deleteRecordsBefore(beforeTimestamp: Long)
}
