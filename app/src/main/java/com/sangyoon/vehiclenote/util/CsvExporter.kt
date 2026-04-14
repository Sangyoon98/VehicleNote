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

@Singleton
class CsvExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
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

    // RFC 4180: 필드를 따옴표로 감싸고, 내부 따옴표는 두 번 반복
    private fun csvField(value: String): String = "\"${value.replace("\"", "\"\"")}\""

    private fun formatTimestamp(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

    private fun fileTimestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}
