package com.sangyoon.vehiclenote.domain.repository

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import kotlinx.coroutines.flow.Flow

interface EntryExitRepository {
    fun getAllRecords(): Flow<List<EntryExitRecord>>
    fun searchRecords(query: String): Flow<List<EntryExitRecord>>
    suspend fun getLastRecordByPlate(plate: String): EntryExitRecord?
    suspend fun getById(id: Long): EntryExitRecord?
    suspend fun insertRecord(record: EntryExitRecord): Long
}
