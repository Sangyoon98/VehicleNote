package com.sangyoon.vehiclenote.domain.model

/**
 * 입출차 기록 보관 기간 설정.
 *
 * [days]가 null이면 기간 제한 없이 영구 보관([UNLIMITED]).
 * [PurgeOldRecordsUseCase]가 이 값을 참조해 오래된 기록을 삭제한다.
 *
 * @property days 보관 일수. null이면 삭제하지 않음.
 */
enum class DataRetentionPeriod(val days: Int?) {
    /** 1일 보관 */
    ONE_DAY(1),
    /** 7일 보관 */
    ONE_WEEK(7),
    /** 30일 보관 */
    ONE_MONTH(30),
    /** 영구 보관 (삭제 안 함) */
    UNLIMITED(null)
}
