package com.sangyoon.vehiclenote.data.mapper

import com.sangyoon.vehiclenote.data.local.entity.VehicleEntity
import com.sangyoon.vehiclenote.domain.model.CustomField
import com.sangyoon.vehiclenote.domain.model.Vehicle
import org.json.JSONArray
import org.json.JSONObject

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

private fun List<CustomField>.toJson(): String {
    val arr = JSONArray()
    forEach { field ->
        arr.put(JSONObject().put("key", field.key).put("value", field.value))
    }
    return arr.toString()
}

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
