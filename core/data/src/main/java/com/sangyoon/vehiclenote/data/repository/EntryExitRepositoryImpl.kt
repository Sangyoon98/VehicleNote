package com.sangyoon.vehiclenote.data.repository

import com.sangyoon.vehiclenote.data.local.dao.EntryExitRecordDao
import com.sangyoon.vehiclenote.data.mapper.toDomain
import com.sangyoon.vehiclenote.data.mapper.toEntity
import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.repository.EntryExitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [EntryExitRepository] 구현체.
 *
 * [EntryExitRecordDao]를 통해 Room DB에 접근한다.
 * [getById]는 차량 정보 JOIN을 포함하는 [EntryExitRecordDao.getByIdWithVehicle]을 사용한다.
 */
@Singleton
class EntryExitRepositoryImpl @Inject constructor(
    private val dao: EntryExitRecordDao
) : EntryExitRepository {

    /** 전체 입출차 기록을 최신 순으로 반환한다. DB 변경 시 자동 갱신. */
    override fun getAllRecords(): Flow<List<EntryExitRecord>> {
        return dao.getAllRecords().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /** 번호판 / 차주명 / 전화번호 통합 검색. */
    override fun searchRecords(query: String): Flow<List<EntryExitRecord>> {
        return dao.searchRecords(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /** 특정 번호판의 가장 최근 기록 조회. 없으면 null. */
    override suspend fun getLastRecordByPlate(plate: String): EntryExitRecord? {
        return dao.getLastByPlate(plate)?.toDomain()
    }

    /**
     * 특정 id 기록을 차량 정보(ownerName)와 함께 조회한다.
     *
     * 내부적으로 [EntryExitRecordDao.getByIdWithVehicle]을 사용하므로 ownerName이 채워진다.
     */
    override suspend fun getById(id: Long): EntryExitRecord? {
        return dao.getByIdWithVehicle(id)?.toDomain()
    }

    /** 입출차 기록을 삽입하고 자동 생성된 id를 반환한다. */
    override suspend fun insertRecord(record: EntryExitRecord): Long {
        return dao.insertRecord(record.toEntity())
    }

    /** 지정 시각 이전의 기록을 일괄 삭제한다. */
    override suspend fun deleteRecordsBefore(timestamp: Long) {
        dao.deleteRecordsBefore(timestamp)
    }
}
