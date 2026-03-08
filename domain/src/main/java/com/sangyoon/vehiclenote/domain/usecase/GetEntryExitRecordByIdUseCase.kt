package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.repository.EntryExitRepository
import javax.inject.Inject

class GetEntryExitRecordByIdUseCase @Inject constructor(
    private val repository: EntryExitRepository
) {
    suspend operator fun invoke(id: Long): EntryExitRecord? = repository.getById(id)
}
