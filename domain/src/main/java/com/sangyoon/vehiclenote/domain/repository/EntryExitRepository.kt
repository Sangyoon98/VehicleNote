package com.sangyoon.vehiclenote.domain.repository

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import kotlinx.coroutines.flow.Flow

/**
 * 입출차 기록 저장소 인터페이스.
 *
 * 구현체는 data 모듈의 [EntryExitRepositoryImpl]이며, Hilt로 바인딩된다.
 */
interface EntryExitRepository {

    /**
     * 전체 입출차 기록을 최신 순으로 반환한다.
     *
     * DB가 변경될 때마다 자동으로 새 목록을 emit하는 핫 스트림.
     */
    fun getAllRecords(): Flow<List<EntryExitRecord>>

    /**
     * 번호판 / 차주명 / 전화번호를 대상으로 입출차 기록을 검색한다.
     *
     * @param query 검색어.
     */
    fun searchRecords(query: String): Flow<List<EntryExitRecord>>

    /**
     * 특정 번호판의 가장 최근 기록을 조회한다.
     *
     * 다음 입출차 유형을 결정(토글)하기 위해 사용된다.
     *
     * @param plate 조회할 차량번호.
     * @return 가장 최근 기록, 없으면 null.
     */
    suspend fun getLastRecordByPlate(plate: String): EntryExitRecord?

    /**
     * 특정 id의 입출차 기록을 단건 조회한다. 차량 정보(ownerName)도 함께 반환된다.
     *
     * @param id 조회할 기록의 PK.
     * @return 해당 기록 또는 null.
     */
    suspend fun getById(id: Long): EntryExitRecord?

    /**
     * 입출차 기록을 DB에 삽입한다.
     *
     * @param record 저장할 기록.
     * @return 삽입 후 자동 생성된 id.
     */
    suspend fun insertRecord(record: EntryExitRecord): Long

    /**
     * 지정된 시각 이전의 기록을 일괄 삭제한다.
     *
     * [PurgeOldRecordsUseCase]에서 보관 기간 초과 기록을 정리할 때 호출된다.
     *
     * @param timestamp 이 시각(epoch ms) 이전 기록을 모두 삭제.
     */
    suspend fun deleteRecordsBefore(timestamp: Long)
}
