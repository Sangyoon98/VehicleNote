package com.sangyoon.vehiclenote.domain.model

/**
 * 입출차 기록 유형.
 *
 * [RecordEntryExitUseCase]가 마지막 상태를 보고 자동으로 토글한다.
 * DB에는 `name` 문자열("ENTRY" / "EXIT")로 저장된다.
 */
enum class RecordType {
    /** 입차 */
    ENTRY,
    /** 출차 */
    EXIT
}
