package com.sangyoon.vehiclenote.ui.entryexitlog

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.RecordType
import com.sangyoon.vehiclenote.ui.components.Plate
import com.sangyoon.vehiclenote.ui.components.PlateSize
import com.sangyoon.vehiclenote.ui.components.VnStatusTag
import com.sangyoon.vehiclenote.ui.components.toTagKind
import com.sangyoon.vehiclenote.ui.theme.VnEntry
import com.sangyoon.vehiclenote.ui.theme.VnEntryBg
import com.sangyoon.vehiclenote.ui.theme.VnEntryInk
import com.sangyoon.vehiclenote.ui.theme.VnExit
import com.sangyoon.vehiclenote.ui.theme.VnExitBg
import com.sangyoon.vehiclenote.ui.theme.VnExitInk
import com.sangyoon.vehiclenote.ui.theme.VnInk
import com.sangyoon.vehiclenote.ui.theme.VnInkMute
import com.sangyoon.vehiclenote.ui.theme.VnLine
import com.sangyoon.vehiclenote.ui.theme.VnPaper
import com.sangyoon.vehiclenote.ui.theme.VnPaperDeep
import com.sangyoon.vehiclenote.ui.theme.VnTypeBody
import com.sangyoon.vehiclenote.ui.theme.VnTypeBodySm
import com.sangyoon.vehiclenote.ui.theme.VnTypeCaption
import com.sangyoon.vehiclenote.ui.theme.VnTypeHeadline
import com.sangyoon.vehiclenote.ui.theme.VnTypeLabel
import com.sangyoon.vehiclenote.ui.theme.VnTypeMonoTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.SortedMap
import java.util.TreeMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryExitLogListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: EntryExitLogListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is EntryExitLogListSideEffect.NavigateToDetail -> onNavigateToDetail(effect.recordId)
                is EntryExitLogListSideEffect.ShareFile -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, effect.uri)
                        putExtra(Intent.EXTRA_SUBJECT, effect.fileName)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "엑셀 파일 내보내기"))
                }
            }
        }
    }

    // Group records by hour label for timeline sticky headers
    val groupedRecords: SortedMap<String, List<EntryExitRecord>> = remember(state.records) {
        TreeMap<String, List<EntryExitRecord>>(Comparator.reverseOrder()).apply {
            state.records.groupBy { record ->
                SimpleDateFormat("HH시", Locale.KOREAN).format(Date(record.timestamp))
            }.forEach { put(it.key, it.value) }
        }
    }

    val entryCount = state.records.count { it.type == RecordType.ENTRY }
    val exitCount = state.records.count { it.type == RecordType.EXIT }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VnPaper)
                .padding(paddingValues),
        ) {
            // ── 검색 필 ────────────────────────────────────────────────────────
            SearchPill(
                query = state.searchQuery,
                isActive = state.isSearchActive,
                isExporting = state.isExporting,
                hasRecords = state.records.isNotEmpty(),
                onQueryChange = { viewModel.onAction(EntryExitLogListAction.SearchQueryChanged(it)) },
                onActiveChange = { viewModel.onAction(EntryExitLogListAction.SearchActiveChanged(it)) },
                onClearQuery = { viewModel.onAction(EntryExitLogListAction.SearchQueryChanged("")) },
                onExportClick = { viewModel.onAction(EntryExitLogListAction.OnExportClicked) },
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            // ── 요약 스트립 ────────────────────────────────────────────────────
            if (state.records.isNotEmpty()) {
                SummaryStrip(
                    totalCount = state.records.size,
                    entryCount = entryCount,
                    exitCount = exitCount,
                    isFilteredToday = state.isFilteredToday,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )
            }

            // ── 타임라인 목록 ──────────────────────────────────────────────────
            if (state.records.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (state.searchQuery.isNotEmpty()) "검색 결과가 없습니다"
                               else "입출차 기록이 없습니다",
                        style = VnTypeBody,
                        color = VnInkMute,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    groupedRecords.forEach { (hourLabel, records) ->
                        stickyHeader(key = "header_$hourLabel") {
                            TimelineHeader(label = hourLabel)
                        }
                        items(items = records, key = { it.id }) { record ->
                            TimelineRow(
                                record = record,
                                onClick = { viewModel.onAction(EntryExitLogListAction.RecordClicked(record.id)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── 검색 필 ──────────────────────────────────────────────────────────────────

@Composable
private fun SearchPill(
    query: String,
    isActive: Boolean,
    isExporting: Boolean,
    hasRecords: Boolean,
    onQueryChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onClearQuery: () -> Unit,
    onExportClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isActive) {
        if (isActive) focusRequester.requestFocus()
        else focusManager.clearFocus()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VnPaperDeep)
            .border(1.dp, VnLine, RoundedCornerShape(12.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = if (isActive) ({ onActiveChange(false) }) else onNavigateBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = if (isActive) "검색 닫기" else "뒤로",
                tint = VnInk,
                modifier = Modifier.size(20.dp),
            )
        }

        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = VnInkMute,
            modifier = Modifier.size(18.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = !isActive) { onActiveChange(true) },
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = VnTypeBody.copy(color = VnInk),
                cursorBrush = SolidColor(VnInk),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = if (isActive) "번호판 검색..."
                                   else "번호판 또는 차주명 검색",
                            style = VnTypeBody,
                            color = VnInkMute,
                        )
                    }
                    inner()
                },
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        if (isActive && query.isNotEmpty()) {
            IconButton(onClick = onClearQuery) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "지우기",
                    tint = VnInkMute,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else if (!isActive) {
            if (isExporting) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            } else {
                IconButton(onClick = onExportClick, enabled = hasRecords) {
                    Icon(
                        Icons.Default.FileDownload,
                        contentDescription = "내보내기",
                        tint = if (hasRecords) VnInk else VnInkMute,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

// ── 요약 스트립 ───────────────────────────────────────────────────────────────

@Composable
private fun SummaryStrip(
    totalCount: Int,
    entryCount: Int,
    exitCount: Int,
    isFilteredToday: Boolean,
    modifier: Modifier = Modifier,
) {
    val dateLabel = if (isFilteredToday) {
        SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN).format(Date()) + " (오늘)"
    } else "전체"

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = dateLabel, style = VnTypeCaption, color = VnInkMute)
            Text(
                text = "총 ${totalCount}건",
                style = VnTypeHeadline,
                color = VnInk,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CountChip(label = "입차", count = entryCount, bg = VnEntryBg, ink = VnEntryInk)
            CountChip(label = "출차", count = exitCount, bg = VnExitBg, ink = VnExitInk)
        }
    }
}

@Composable
private fun CountChip(label: String, count: Int, bg: Color, ink: Color) {
    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = VnTypeCaption, color = ink)
        Text(text = count.toString(), style = VnTypeLabel, color = ink)
    }
}

// ── 타임라인 헤더 (sticky) ────────────────────────────────────────────────────

@Composable
private fun TimelineHeader(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(VnPaper)
            .padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Text(text = label, style = VnTypeCaption, color = VnInkMute)
    }
}

// ── 타임라인 행 ───────────────────────────────────────────────────────────────

@Composable
private fun TimelineRow(
    record: EntryExitRecord,
    onClick: () -> Unit,
) {
    val isEntry = record.type == RecordType.ENTRY
    val barColor = if (isEntry) VnEntry else VnExit
    val timeText = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(record.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 시각 (44dp fixed width, mono)
        Text(
            text = timeText.substring(0, 5), // HH:mm
            style = VnTypeMonoTime,
            color = VnInkMute,
            modifier = Modifier.width(44.dp),
        )

        // 2dp 컬러 바
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(36.dp)
                .background(barColor, RoundedCornerShape(1.dp)),
        )

        // 번호판
        Plate(
            value = record.licensePlate,
            size = PlateSize.Sm,
        )

        Spacer(modifier = Modifier.weight(1f))

        // 입/출차 태그
        VnStatusTag(
            kind = record.type.toTagKind(),
            text = if (isEntry) "입차" else "출차",
        )
    }
}
