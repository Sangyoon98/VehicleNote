package com.sangyoon.vehiclenote.domain.model

/**
 * 입출차 기록 도메인 모델.
 *
 * @property id Room DB 자동 생성 PK. 신규 기록 시 0으로 설정.
 * @property licensePlate 차량번호.
 * @property type 기록 유형 ([RecordType.ENTRY] 입차 / [RecordType.EXIT] 출차).
 * @property timestamp 기록 시각 (epoch milliseconds).
 * @property vehicleId 연결된 [Vehicle]의 id. 미등록 차량이면 null.
 * @property ownerName DB JOIN 결과에서 채워지는 차주명. 조회 시에만 유효하며 INSERT 시 무시됨.
 */
data class EntryExitRecord(
    val id: Long = 0,
    val licensePlate: String,
    val type: RecordType,
    val timestamp: Long = System.currentTimeMillis(),
    val vehicleId: Long? = null,
    // JOIN 결과에서 채워지는 필드 (optional)
    val ownerName: String? = null
)
