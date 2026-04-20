package com.sangyoon.vehiclenote.data.local.entity

/**
 * 입출차 기록 검색 쿼리의 플랫(flat) 결과를 받기 위한 프로젝션 클래스.
 *
 * Room `@Relation`은 내부적으로 별도 쿼리를 수행하므로 SQL `WHERE`가 적용되지 않는 문제가 있다.
 * 이를 우회하기 위해 LEFT JOIN을 포함한 단일 쿼리를 실행하고 결과를 이 클래스로 직접 매핑한다.
 *
 * @property id 기록 PK.
 * @property licensePlate 차량번호.
 * @property type 기록 유형 문자열 ("ENTRY"/"EXIT").
 * @property timestamp 기록 시각 (epoch ms).
 * @property vehicleId 연결된 차량 id (null이면 미등록).
 * @property ownerName JOIN으로 가져온 차주명 (차량 미등록 시 null).
 */
data class SearchResultTuple(
    val id: Long,
    val licensePlate: String,
    val type: String,
    val timestamp: Long,
    val vehicleId: Long?,
    val ownerName: String?
)
