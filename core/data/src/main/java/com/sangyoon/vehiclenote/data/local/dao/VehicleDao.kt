package com.sangyoon.vehiclenote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sangyoon.vehiclenote.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

/**
 * 차량 테이블(`vehicles`) Room DAO.
 *
 * 읽기 작업은 [Flow]를 반환해 DB 변경 시 자동으로 UI를 갱신한다.
 */
@Dao
interface VehicleDao {

    /**
     * 전체 차량 목록을 최신 등록 순으로 반환한다.
     */
    @Query("SELECT * FROM vehicles ORDER BY createdAt DESC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    /**
     * 차량번호 / 차주명 / 부서 / 전화번호를 대상으로 부분 일치 검색한다.
     *
     * @param query 검색어. SQL LIKE 패턴으로 변환되어 사용됨.
     */
    @Query("""
        SELECT * FROM vehicles
        WHERE licensePlate LIKE '%' || :query || '%'
           OR ownerName   LIKE '%' || :query || '%'
           OR department  LIKE '%' || :query || '%'
           OR phoneNumber LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun search(query: String): Flow<List<VehicleEntity>>

    /**
     * 특정 id의 차량을 단건 조회한다.
     *
     * @param id 조회할 차량의 PK.
     * @return 해당 차량 엔티티, 없으면 null.
     */
    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: Long): VehicleEntity?

    /**
     * 새 차량을 삽입한다. id 충돌 시 기존 행을 대체(REPLACE)한다.
     *
     * @param vehicle 삽입할 차량 엔티티.
     * @return 자동 생성된 id.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    /**
     * 기존 차량 정보를 수정한다.
     *
     * @param vehicle 수정할 차량 엔티티 (id로 행을 식별).
     */
    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    /**
     * 차량을 삭제한다.
     *
     * @param vehicle 삭제할 차량 엔티티.
     */
    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    /**
     * 번호판 문자열로 차량을 조회한다.
     *
     * @param plate 조회할 차량번호 (정확히 일치).
     * @return 해당 차량 엔티티, 없으면 null.
     */
    @Query("SELECT * FROM vehicles WHERE licensePlate = :plate LIMIT 1")
    suspend fun getByLicensePlate(plate: String): VehicleEntity?
}
