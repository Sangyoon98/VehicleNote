package com.sangyoon.vehiclenote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 차량 정보 Room DB 엔티티. 테이블명: `vehicles`.
 *
 * [department]는 DB 스키마 v1 호환을 위해 NOT NULL로 유지한다.
 * 부서 미지정인 경우 빈 문자열("")로 저장되며, 도메인 변환 시 null로 매핑된다.
 *
 * [customFields]는 [CustomField] 리스트를 JSON 배열 문자열로 직렬화한 값이다.
 *
 * @property id 자동 생성 PK.
 * @property licensePlate 차량번호 (NOT NULL).
 * @property ownerName 차주명 (NOT NULL).
 * @property department 소속 부서. NOT NULL이지만 빈 문자열이면 미지정 처리.
 * @property phoneNumber 전화번호 (nullable).
 * @property carModel 차종 (nullable).
 * @property memo 메모 (nullable).
 * @property photoUri 내부 저장소 사진 파일 절대 경로 (nullable).
 * @property customFields 커스텀 필드 JSON 배열 문자열. 기본값 `'[]'`.
 * @property createdAt 등록 시각 (epoch ms).
 * @property updatedAt 마지막 수정 시각 (epoch ms).
 */
@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val licensePlate: String,
    val ownerName: String,
    val department: String,       // DB에서 NOT NULL 유지 (v1 호환). 빈 문자열이면 "미지정" 처리
    val phoneNumber: String?,
    val carModel: String?,
    val memo: String?,
    val photoUri: String?,
    val customFields: String,
    val createdAt: Long,
    val updatedAt: Long
)
