package com.sangyoon.vehiclenote.domain.model

/**
 * 차량에 첨부하는 사용자 정의 필드.
 *
 * 화면에서 동적으로 추가/제거할 수 있으며, [Vehicle.customFields]에 리스트로 저장된다.
 * DB에는 JSON 배열 문자열로 직렬화되어 단일 컬럼에 저장된다.
 *
 * @property key 필드명. 예: "차고지", "보험사"
 * @property value 필드값. 예: "지하 1층", "삼성화재"
 */
data class CustomField(
    val key: String,
    val value: String
)
