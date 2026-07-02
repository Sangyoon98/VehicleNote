package com.sangyoon.vehiclenote.feature.settings

import android.content.Context
import android.net.Uri
import com.sangyoon.vehiclenote.domain.model.CustomField
import com.sangyoon.vehiclenote.domain.model.Vehicle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CSV 파일을 파싱해 Vehicle 목록을 반환합니다.
 *
 * 지원 포맷: VehicleCsvExporter가 생성한 CSV (COLUMN_VERSION 1)
 * - 첫 줄: 헤더 (번호판,차주명,부서,전화번호,차종,메모,커스텀필드)
 * - UTF-8 BOM 자동 제거
 * - 필수: 번호판(0), 차주명(1) — 비어있으면 해당 행 skip
 * - photoPath: CSV에 포함되지 않으므로 null로 설정
 *
 * ⚠️ VehicleCsvExporter의 COLUMN_VERSION이 올라가면 컬럼 인덱스 확인 필요.
 */
@Singleton
class VehicleCsvParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class ParseResult(
        val vehicles: List<Vehicle>,
        val skippedRows: Int    // 필수 필드 누락으로 건너뛴 행 수
    )

    suspend fun parse(uri: Uri): ParseResult = withContext(Dispatchers.IO) {
        parseInternal(uri)
    }

    private fun parseInternal(uri: Uri): ParseResult {
        val lines = context.contentResolver.openInputStream(uri)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { reader ->
                reader.readLines().toMutableList().also { list ->
                    // UTF-8 BOM 제거
                    if (list.isNotEmpty()) {
                        list[0] = list[0].trimStart('\uFEFF')
                    }
                }
            } ?: return ParseResult(emptyList(), 0)

        if (lines.size < 2) return ParseResult(emptyList(), 0)

        val dataLines = lines.drop(1) // 헤더 skip
        val vehicles = mutableListOf<Vehicle>()
        var skipped = 0

        dataLines.forEach { line ->
            if (line.isBlank()) return@forEach
            val cols = parseCsvLine(line)
            val plate = cols.getOrNull(0)?.trim() ?: ""
            val owner = cols.getOrNull(1)?.trim() ?: ""
            if (plate.isEmpty() || owner.isEmpty()) {
                skipped++
                return@forEach
            }
            vehicles.add(
                Vehicle(
                    licensePlate = plate,
                    ownerName = owner,
                    department = cols.getOrNull(2)?.trim()?.takeIf { it.isNotEmpty() },
                    phoneNumber = cols.getOrNull(3)?.trim()?.takeIf { it.isNotEmpty() },
                    carModel = cols.getOrNull(4)?.trim()?.takeIf { it.isNotEmpty() },
                    memo = cols.getOrNull(5)?.trim()?.takeIf { it.isNotEmpty() },
                    customFields = parseCustomFields(cols.getOrNull(6) ?: "[]"),
                    photoPath = null  // CSV에 사진 미포함
                )
            )
        }

        return ParseResult(vehicles, skipped)
    }

    private fun parseCustomFields(json: String): List<CustomField> {
        return try {
            val array = JSONArray(json.ifBlank { "[]" })
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                CustomField(key = obj.getString("key"), value = obj.getString("value"))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * RFC 4180 CSV 한 행 파싱.
     * 따옴표 감싼 필드와 이중 따옴표 이스케이프를 올바르게 처리합니다.
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++ // 이중 따옴표 → 하나로
                    } else {
                        inQuotes = false
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
