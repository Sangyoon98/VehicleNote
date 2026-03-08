package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.repository.EntryExitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEntryExitRecordsUseCase @Inject constructor(
    private val repository: EntryExitRepository
) {
    operator fun invoke(): Flow<List<EntryExitRecord>> = repository.getAllRecords()
}
