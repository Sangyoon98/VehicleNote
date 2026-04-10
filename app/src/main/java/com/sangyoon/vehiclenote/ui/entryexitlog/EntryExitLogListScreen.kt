package com.sangyoon.vehiclenote.ui.entryexitlog

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.RecordType
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

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is EntryExitLogListSideEffect.NavigateToDetail -> onNavigateToDetail(effect.recordId)
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
                                IconButton(onClick = { viewModel.onAction(EntryExitLogListAction.SearchActiveChanged(true)) }) {
                                    Icon(Icons.Default.Search, contentDescription = "검색")
                                }
                            }
                        }
                    )
                },
                expanded = state.isSearchActive,
                onExpandedChange = { viewModel.onAction(EntryExitLogListAction.SearchActiveChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.licensePlate,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatTimestamp(record.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val isEntry = record.type == RecordType.ENTRY
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isEntry) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                Text(
                    text = if (isEntry) "입차완료" else "출차완료",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEntry) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
