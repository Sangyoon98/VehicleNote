package com.sangyoon.vehiclenote.domain.repository

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeEntryExitRepository : EntryExitRepository {
    private val records = mutableListOf<EntryExitRecord>()
    private val _flow = MutableStateFlow<List<EntryExitRecord>>(emptyList())
    private var nextId = 1L

    var deleteBeforeTimestamp: Long? = null
        private set

    override fun getAllRecords(): Flow<List<EntryExitRecord>> = _flow

    override fun searchRecords(query: String): Flow<List<EntryExitRecord>> =
        _flow.map { list -> list.filter { it.licensePlate.contains(query) } }

    override suspend fun getLastRecordByPlate(plate: String): EntryExitRecord? =
        records.filter { it.licensePlate == plate }.maxByOrNull { it.timestamp }

    override suspend fun getById(id: Long): EntryExitRecord? = records.find { it.id == id }

    override suspend fun insertRecord(record: EntryExitRecord): Long {
        val id = nextId++
        records.add(record.copy(id = id))
        _flow.value = records.toList()
        return id
    }

    override suspend fun deleteRecordsBefore(timestamp: Long) {
        deleteBeforeTimestamp = timestamp
        records.removeAll { it.timestamp < timestamp }
        _flow.value = records.toList()
    }

    fun clear() {
        records.clear()
        _flow.value = emptyList()
        nextId = 1L
        deleteBeforeTimestamp = null
    }
}
