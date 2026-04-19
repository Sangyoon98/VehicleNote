package com.sangyoon.vehiclenote.domain.model

/**
 * 차량 정보 도메인 모델.
 *
 * @property id Room DB 자동 생성 PK. 신규 등록 시 0으로 설정.
 * @property licensePlate 차량번호 (필수). 예: "12가 3456"
 * @property ownerName 차주명 (필수).
 * @property department 소속 부서. null이면 미지정.
 * @property phoneNumber 차주 전화번호. null이면 미입력.
 * @property carModel 차종/모델명. null이면 미입력.
 * @property memo 메모. null이면 미입력.
 * @property photoPath 내부 저장소의 사진 파일 절대 경로. null이면 사진 없음.
 * @property customFields 사용자 정의 추가 필드 목록. 기본값 빈 리스트.
 * @property createdAt 등록 시각 (epoch milliseconds).
 * @property updatedAt 마지막 수정 시각 (epoch milliseconds).
 */
data class Vehicle(
    val id: Long = 0,
    val licensePlate: String,
    val ownerName: String,
    val department: String? = null,
    val phoneNumber: String? = null,
    val carModel: String? = null,
    val memo: String? = null,
    val photoPath: String? = null,
    val customFields: List<CustomField> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
