package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.repository.EntryExitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 전체 입출차 기록을 실시간으로 조회하는 UseCase.
 *
 * DB 변경 시 자동으로 새 목록을 emit하는 핫 스트림을 반환한다.
 */
class GetEntryExitRecordsUseCase @Inject constructor(
    private val repository: EntryExitRepository
) {
    /**
     * @return 최신 순으로 정렬된 입출차 기록 목록 [Flow].
     */
    operator fun invoke(): Flow<List<EntryExitRecord>> = repository.getAllRecords()
}
