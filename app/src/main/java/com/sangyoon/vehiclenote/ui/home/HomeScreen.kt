package com.sangyoon.vehiclenote.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.ui.home.components.RecentVehicleCard
import com.sangyoon.vehiclenote.ui.home.components.StatisticsSection
import com.sangyoon.vehiclenote.ui.home.components.VehicleListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = state.searchQuery,
                        onQueryChange = { viewModel.onIntent(HomeIntent.SearchQueryChanged(it)) },
                        onSearch = { viewModel.onIntent(HomeIntent.SearchQueryChanged(it)) },
                        expanded = state.isSearchActive,
                        onExpandedChange = { viewModel.onIntent(HomeIntent.SearchActiveChanged(it)) },
                        placeholder = { Text("차량번호 검색") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onIntent(HomeIntent.SearchQueryChanged("")) }) {
                                    Icon(Icons.Default.Close, contentDescription = "지우기")
                                }
                            }
                        }
                    )
                },
                expanded = state.isSearchActive,
                onExpandedChange = { viewModel.onIntent(HomeIntent.SearchActiveChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (!state.isSearchActive) 16.dp else 0.dp),
            ) {
                // 검색 활성화 시 검색 결과만 표시
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.vehicles) { vehicle ->
                        VehicleListItem(
                            vehicle = vehicle,
                            onClick = {
                                onNavigateToDetail(vehicle.id)
                                viewModel.onIntent(HomeIntent.SearchActiveChanged(false))
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!state.isSearchActive) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAdd,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("차량 등록") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    Text(
                        text = state.error ?: "오류 발생",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    HomeContent(
                        state = state,
                        onVehicleClick = onNavigateToDetail,
                        onDeleteVehicle = { viewModel.onIntent(HomeIntent.DeleteVehicle(it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    onVehicleClick: (Long) -> Unit,
    onDeleteVehicle: (Vehicle) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 통계 섹션
        item {
            StatisticsSection(
                totalCount = state.totalVehicleCount,
                todayCount = state.todayRegisteredCount,
                departmentStats = state.departmentStats
            )
        }

        // 최근 등록 차량
        if (state.recentVehicles.isNotEmpty()) {
            item {
                Text(
                    text = "최근 등록 차량",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recentVehicles) { vehicle ->
                        RecentVehicleCard(
                            vehicle = vehicle,
                            onClick = { onVehicleClick(vehicle.id) }
                        )
                    }
                }
            }
        }

        // 전체 차량 목록
        item {
            Text(
                text = "전체 챠량 (${state.vehicles.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(
            items = state.vehicles,
            key = { it.id }
        ) { vehicle ->
            VehicleListItem(
                vehicle = vehicle,
                onClick = { onVehicleClick(vehicle.id) },
                onDelete = { onDeleteVehicle(vehicle) }
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onNavigateToAdd = {},
        onNavigateToDetail = {}
    )
}