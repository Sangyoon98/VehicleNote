package com.sangyoon.vehiclenote.feature.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.sangyoon.vehiclenote.domain.model.Vehicle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Vehicle 목록을 CSV 파일로 내보냅니다.
 *
 * CSV 컬럼 (v1): 번호판, 차주명, 부서, 전화번호, 차종, 메모, 커스텀필드
 * - 제외 항목: id(자동생성), photoPath(미지원), createdAt/updatedAt(시스템 필드)
 * - 커스텀필드: org.json JSONArray 형식 문자열
 * - 인코딩: UTF-8 BOM (Excel 한국어 깨짐 방지)
 * - 필드 포맷: RFC 4180 (따옴표 감쌈, 내부 따옴표 두 번 반복)
 *
 * ⚠️ DB 스키마 변경 시 COLUMN_VERSION 및 컬럼 목록을 함께 수정하세요.
 */
@Singleton
class VehicleCsvExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val COLUMN_VERSION = 1
        val HEADERS = listOf("번호판", "차주명", "부서", "전화번호", "차종", "메모", "커스텀필드")
    }

    suspend fun export(vehicles: List<Vehicle>): Uri = withContext(Dispatchers.IO) {
        val exportsDir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val fileName = "vehicles_${fileTimestamp()}.csv"
        val file = File(exportsDir, fileName)

        file.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write("\uFEFF") // UTF-8 BOM
            writer.write(HEADERS.joinToString(",") { csvField(it) })
            writer.write("\n")
            vehicles.forEach { vehicle ->
                writer.write(toRow(vehicle))
                writer.write("\n")
            }
        }

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun toRow(vehicle: Vehicle): String {
        val customFieldsJson = JSONArray().apply {
            vehicle.customFields.forEach { field ->
                put(JSONObject().apply {
                    put("key", field.key)
                    put("value", field.value)
                })
            }
        }.toString()

        return listOf(
            vehicle.licensePlate,
            vehicle.ownerName,
            vehicle.department ?: "",
            vehicle.phoneNumber ?: "",
            vehicle.carModel ?: "",
            vehicle.memo ?: "",
            customFieldsJson
        ).joinToString(",") { csvField(it) }
    }

    private fun csvField(value: String): String = "\"${value.replace("\"", "\"\"")}\""

    private fun fileTimestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}
