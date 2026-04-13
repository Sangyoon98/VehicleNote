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

@Singleton
class EntryExitRepositoryImpl @Inject constructor(
    private val dao: EntryExitRecordDao
) : EntryExitRepository {

    override fun getAllRecords(): Flow<List<EntryExitRecord>> {
        return dao.getAllRecords().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchRecords(query: String): Flow<List<EntryExitRecord>> {
        return dao.searchRecords(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLastRecordByPlate(plate: String): EntryExitRecord? {
        return dao.getLastByPlate(plate)?.toDomain()
    }

    override suspend fun getById(id: Long): EntryExitRecord? {
        return dao.getByIdWithVehicle(id)?.toDomain()
    }

    override suspend fun insertRecord(record: EntryExitRecord): Long {
        return dao.insertRecord(record.toEntity())
    }

    override suspend fun deleteRecordsBefore(timestamp: Long) {
        dao.deleteRecordsBefore(timestamp)
    }
}
