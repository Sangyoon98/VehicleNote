package com.sangyoon.vehiclenote.data.mapper

import com.sangyoon.vehiclenote.data.local.entity.VehicleEntity
import com.sangyoon.vehiclenote.domain.model.CustomField
import com.sangyoon.vehiclenote.domain.model.Vehicle
import org.json.JSONArray
import org.json.JSONObject

/**
 * [VehicleEntity]를 도메인 모델 [Vehicle]로 변환한다.
 *
 * - [VehicleEntity.department]가 빈 문자열이면 [Vehicle.department]를 null로 변환한다.
 * - [VehicleEntity.customFields] JSON 문자열을 [CustomField] 리스트로 역직렬화한다.
 */
fun VehicleEntity.toDomain(): Vehicle {
    return Vehicle(
        id = id,
        licensePlate = licensePlate,
        ownerName = ownerName,
        department = department.ifBlank { null },   // 빈 문자열 → null로 변환
        phoneNumber = phoneNumber,
        carModel = carModel,
        memo = memo,
        photoPath = photoUri,
        customFields = customFields.toCustomFields(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * 도메인 모델 [Vehicle]을 [VehicleEntity]로 변환한다.
 *
 * - [Vehicle.department]가 null이면 빈 문자열로 저장한다 (DB NOT NULL 제약 유지).
 * - [Vehicle.customFields]를 JSON 배열 문자열로 직렬화한다.
 */
fun Vehicle.toEntity(): VehicleEntity {
    return VehicleEntity(
        id = id,
        licensePlate = licensePlate,
        ownerName = ownerName,
        department = department ?: "",              // null → 빈 문자열로 저장
        phoneNumber = phoneNumber,
        carModel = carModel,
        memo = memo,
        photoUri = photoPath,
        customFields = customFields.toJson(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * [CustomField] 리스트를 JSON 배열 문자열로 직렬화한다.
 *
 * 결과 형식: `[{"key":"차고지","value":"지하 1층"}, ...]`
 */
private fun List<CustomField>.toJson(): String {
    val arr = JSONArray()
    forEach { field ->
        arr.put(JSONObject().put("key", field.key).put("value", field.value))
    }
    return arr.toString()
}

/**
 * JSON 배열 문자열을 [CustomField] 리스트로 역직렬화한다.
 *
 * 파싱 실패 시 빈 리스트를 반환한다.
 */
private fun String.toCustomFields(): List<CustomField> {
    return try {
        val arr = JSONArray(this)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            CustomField(key = obj.getString("key"), value = obj.getString("value"))
        }
    } catch (e: Exception) {
        emptyList()
    }
}
