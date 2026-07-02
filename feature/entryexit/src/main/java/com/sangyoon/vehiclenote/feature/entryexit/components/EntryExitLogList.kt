package com.sangyoon.vehiclenote.feature.entryexit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.core.ui.components.Plate
import com.sangyoon.vehiclenote.core.ui.components.PlateSize
import com.sangyoon.vehiclenote.core.ui.components.VnStatusTag
import com.sangyoon.vehiclenote.core.ui.components.toTagKind
import com.sangyoon.vehiclenote.core.ui.theme.VnTypeMonoTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * modifier를 LazyColumn에 직접 적용 — Column 래퍼 제거.
 *
 * BottomSheetScaffold의 sheetContent(ColumnScope)에서 Modifier.weight(1f)로 호출하면
 * LazyColumn이 NestedScrollConnection 체인에 직접 연결되어
 * 시트 확장/축소 ↔ 리스트 스크롤 우선순위가 자연스럽게 동작한다.
 */
@Composable
fun EntryExitLogList(
    records: List<EntryExitRecord>,
    onRecordClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
) {
    LazyColumn(
        modifier = modifier,
        userScrollEnabled = userScrollEnabled,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(contentType = "section_header") {
            Text(
                text = "최근 인식 기록",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        items(
            items = records,
            key = { it.id },
            contentType = { "record_item" },
        ) { record ->
            EntryExitLogItem(
                record = record,
                onClick = { onRecordClick(record.id) },
            )
        }
    }
}

@Composable
private fun EntryExitLogItem(
    record: EntryExitRecord,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Plate(value = record.licensePlate, size = PlateSize.Sm)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatTimestamp(record.timestamp),
                    style = VnTypeMonoTime,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            VnStatusTag(
                kind = record.type.toTagKind(),
                text = if (record.type == com.sangyoon.vehiclenote.domain.model.RecordType.ENTRY) "입차" else "출차",
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
