package com.sangyoon.vehiclenote.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.RecordType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 입출차 기록 목록을 CSV 파일로 내보내는 유틸리티.
 *
 * 생성된 파일은 앱 캐시 디렉터리(`cache/exports/`)에 저장되며,
 * FileProvider Uri를 통해 외부 앱(파일 관리자, 이메일 등)과 공유할 수 있다.
 *
 * CSV 형식:
 * - 인코딩: UTF-8 BOM (Excel 한국어 깨짐 방지)
 * - 컬럼: 번호판, 유형(입차/출차), 일시(yyyy-MM-dd HH:mm:ss), 차주명
 * - 필드 포맷: RFC 4180 (따옴표 감쌈, 내부 따옴표는 두 번 반복)
 */
@Singleton
class CsvExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 입출차 기록 목록을 CSV 파일로 내보내고 공유용 Uri를 반환한다.
     *
     * @param records 내보낼 입출차 기록 목록.
     * @return FileProvider가 발급한 공유용 콘텐츠 Uri.
     */
    fun export(records: List<EntryExitRecord>): Uri {
        val exportsDir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val fileName = "entry_exit_${fileTimestamp()}.csv"
        val file = File(exportsDir, fileName)

        file.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write("\uFEFF") // UTF-8 BOM — Excel 한국어 깨짐 방지
            writer.write("${csvField("번호판")},${csvField("유형")},${csvField("일시")},${csvField("차주명")}\n")
            records.forEach { record ->
                writer.write(
                    "${csvField(record.licensePlate)}," +
                    "${csvField(if (record.type == RecordType.ENTRY) "입차" else "출차")}," +
                    "${csvField(formatTimestamp(record.timestamp))}," +
                    "${csvField(record.ownerName ?: "")}\n"
                )
            }
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * RFC 4180: 필드를 따옴표로 감싸고, 내부 따옴표는 두 번 반복해 이스케이프한다.
     */
    private fun csvField(value: String): String = "\"${value.replace("\"", "\"\"")}\""

    private fun formatTimestamp(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

    private fun fileTimestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}
