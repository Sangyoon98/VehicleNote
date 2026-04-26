package com.sangyoon.vehiclenote.ui.entryexitlog

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.RecordType
import com.sangyoon.vehiclenote.ui.components.Plate
import com.sangyoon.vehiclenote.ui.components.PlateSize
import com.sangyoon.vehiclenote.ui.components.VnStatusTag
import com.sangyoon.vehiclenote.ui.components.toTagKind
import com.sangyoon.vehiclenote.ui.theme.VnInkMute
import com.sangyoon.vehiclenote.ui.theme.VnTypeMonoTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    Scaffold(
        topBar = {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = state.searchQuery,
                        onQueryChange = { viewModel.onAction(EntryExitLogListAction.SearchQueryChanged(it)) },
                        onSearch = { viewModel.onAction(EntryExitLogListAction.SearchQueryChanged(it)) },
                        expanded = state.isSearchActive,
                        onExpandedChange = { viewModel.onAction(EntryExitLogListAction.SearchActiveChanged(it)) },
                        placeholder = {
                            Text(
                                if (state.isFilteredToday) "오늘 입출차 기록 검색"
                                else "번호판 또는 차주명 검색"
                            )
                        },
                        leadingIcon = {
                            if (state.isSearchActive) {
                                IconButton(onClick = { viewModel.onAction(EntryExitLogListAction.SearchActiveChanged(false)) }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                                }
                            } else {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                                }
                            }
                        },
                        trailingIcon = {
                            if (state.isSearchActive && state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onAction(EntryExitLogListAction.SearchQueryChanged("")) }) {
                                    Icon(Icons.Default.Close, contentDescription = "지우기")
                                }
                            } else if (!state.isSearchActive) {
                                Row {
                                    if (state.isExporting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.CenterVertically),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        IconButton(
                                            onClick = { viewModel.onAction(EntryExitLogListAction.OnExportClicked) },
                                            enabled = state.records.isNotEmpty()
                                        ) {
                                            Icon(Icons.Default.FileDownload, contentDescription = "엑셀 내보내기")
                                        }
                                    }
                                    IconButton(onClick = { viewModel.onAction(EntryExitLogListAction.SearchActiveChanged(true)) }) {
                                        Icon(Icons.Default.Search, contentDescription = "검색")
                                    }
                                }
                            }
                        }
                    )
                },
                expanded = state.isSearchActive,
                onExpandedChange = { viewModel.onAction(EntryExitLogListAction.SearchActiveChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = if (state.isSearchActive) 0.dp else 16.dp,
                        start = if (state.isSearchActive) 0.dp else 16.dp,
                        end = if (state.isSearchActive) 0.dp else 16.dp,
                    ),
            ) {
                // SearchBar가 expanded일 때 결과를 여기에 표시
                EntryExitRecordList(
                    records = state.records,
                    searchQuery = state.searchQuery,
                    onRecordClick = { viewModel.onAction(EntryExitLogListAction.RecordClicked(it)) },
                )
            }
        }
    ) { paddingValues ->
        // SearchBar가 collapsed일 때 전체 목록 표시
        EntryExitRecordList(
            records = state.records,
            searchQuery = state.searchQuery,
            onRecordClick = { viewModel.onAction(EntryExitLogListAction.RecordClicked(it)) },
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun EntryExitRecordList(
    records: List<EntryExitRecord>,
    searchQuery: String,
    onRecordClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (records.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (searchQuery.isNotEmpty()) "검색 결과가 없습니다" else "입출차 기록이 없습니다",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    text = "총 ${records.size}건",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            items(items = records, key = { it.id }) { record ->
                EntryExitLogItem(
                    record = record,
                    onClick = { onRecordClick(record.id) },
                )
            }
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
                    color = VnInkMute,
                )
            }

            VnStatusTag(
                kind = record.type.toTagKind(),
                text = if (record.type == RecordType.ENTRY) "입차" else "출차",
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
