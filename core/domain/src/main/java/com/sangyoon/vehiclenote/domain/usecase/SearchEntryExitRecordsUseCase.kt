package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.repository.EntryExitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 번호판 / 차주명 / 전화번호로 입출차 기록을 검색하는 UseCase.
 */
class SearchEntryExitRecordsUseCase @Inject constructor(
    private val repository: EntryExitRepository
) {
    /**
     * @param query 검색어.
     * @return 검색 결과 입출차 기록 목록 [Flow].
     */
    operator fun invoke(query: String): Flow<List<EntryExitRecord>> =
        repository.searchRecords(query)
}
