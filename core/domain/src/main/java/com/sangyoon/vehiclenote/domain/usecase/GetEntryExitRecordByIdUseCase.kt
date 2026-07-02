package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.repository.EntryExitRepository
import javax.inject.Inject

/**
 * 특정 id의 입출차 기록을 단건 조회하는 UseCase.
 *
 * 내부적으로 차량 정보 JOIN을 포함하므로 [EntryExitRecord.ownerName]도 채워진다.
 */
class GetEntryExitRecordByIdUseCase @Inject constructor(
    private val repository: EntryExitRepository
) {
    /**
     * @param id 조회할 기록의 PK.
     * @return 해당 기록 (차량 정보 포함), 없으면 null.
     */
    suspend operator fun invoke(id: Long): EntryExitRecord? = repository.getById(id)
}
