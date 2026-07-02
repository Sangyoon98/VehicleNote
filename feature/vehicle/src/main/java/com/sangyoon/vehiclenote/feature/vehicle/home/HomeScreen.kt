package com.sangyoon.vehiclenote.feature.vehicle.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.feature.vehicle.home.components.RecentVehicleCard
import com.sangyoon.vehiclenote.feature.vehicle.home.components.StatisticsSection
import com.sangyoon.vehiclenote.feature.vehicle.home.components.VehicleListItem
import com.sangyoon.vehiclenote.core.ui.theme.AppTheme
import com.sangyoon.vehiclenote.core.ui.theme.VnDanger
import kotlinx.coroutines.launch

private val SearchBarHeight = 56.dp
private val SearchBarSpacing = 16.dp  // 서치바 위아래 간격

// 콘텐츠 상단 오프셋: 간격 + 서치바 높이 + 간격
private val ContentTopOffset = SearchBarSpacing + SearchBarHeight + SearchBarSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    parentContentPadding: PaddingValues = PaddingValues(),
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToTodayRecords: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is HomeSideEffect.NavigateToAdd -> onNavigateToAdd()
                is HomeSideEffect.NavigateToDetail -> onNavigateToDetail(effect.vehicleId)
                is HomeSideEffect.ShowSnackbar -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
                is HomeSideEffect.ScrollToFilter -> {
                    coroutineScope.launch {
                        // 통계(0) + 최근차량 헤더(1) + 최근차량 행(2) + 목록 헤더(3) + 칩(4)
                        // 최근차량 없을 경우: 통계(0) + 목록 헤더(1) + 칩(2)
                        val chipsIndex = if (state.recentVehicles.isNotEmpty()) 4 else 2
                        listState.animateScrollToItem(chipsIndex)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!state.isSearchActive) {
                val systemNavBar =
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                val bottomNavBarHeight =
                    (parentContentPadding.calculateBottomPadding() - systemNavBar).coerceAtLeast(0.dp)
                FloatingActionButton(
                    onClick = { viewModel.onAction(HomeAction.AddVehicleClicked) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = bottomNavBarHeight),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "차량 등록")
                }
            }
        }
    ) { paddingValues ->
        // paddingValues를 Box에 적용해 시스템 인셋(상태바·내비바·FAB 여백)을 한 번에 처리
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                        listState = listState,
                        contentPadding = PaddingValues(
                            top = paddingValues.calculateTopPadding() + ContentTopOffset,
                            bottom = maxOf(
                                paddingValues.calculateBottomPadding(),
                                parentContentPadding.calculateBottomPadding()
                            ) + 16.dp,
                        ),
                        onVehicleClick = { viewModel.onAction(HomeAction.VehicleClicked(it)) },
                        onDeleteVehicle = { viewModel.onAction(HomeAction.DeleteVehicle(it)) },
                        onDepartmentFilterSelected = {
                            viewModel.onAction(HomeAction.DepartmentFilterSelected(it))
                        },
                        onDepartmentClickFromStats = { viewModel.selectDepartmentFromStats(it) },
                        onTotalCountClick = {
                            coroutineScope.launch {
                                // 통계(0) + 최근차량 헤더(1) + 최근차량 행(2) + 목록 헤더(3)
                                // 최근차량 없을 경우: 통계(0) + 목록 헤더(1)
                                val listHeaderIndex = if (state.recentVehicles.isNotEmpty()) 3 else 1
                                listState.animateScrollToItem(listHeaderIndex)
                            }
                        },
                        onTodayCountClick = onNavigateToTodayRecords
                    )
                }
            }

            // 플로팅 서치바: 시스템 인셋은 Box에서 이미 처리됐으므로 16dp만
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = state.searchQuery,
                        onQueryChange = { viewModel.onAction(HomeAction.SearchQueryChanged(it)) },
                        onSearch = { viewModel.onAction(HomeAction.SearchQueryChanged(it)) },
                        expanded = state.isSearchActive,
                        onExpandedChange = { viewModel.onAction(HomeAction.SearchActiveChanged(it)) },
                        placeholder = { Text("차량 번호로 차량 검색") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    viewModel.onAction(HomeAction.SearchQueryChanged(""))
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "지우기")
                                }
                            }
                        }
                    )
                },
                expanded = state.isSearchActive,
                onExpandedChange = { viewModel.onAction(HomeAction.SearchActiveChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = if (state.isSearchActive) 0.dp else SearchBarSpacing,
                        start = if (state.isSearchActive) 0.dp else SearchBarSpacing,
                        end = if (state.isSearchActive) 0.dp else SearchBarSpacing,
                    )
                    .align(Alignment.TopCenter),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.vehicles) { vehicle ->
                        VehicleListItem(
                            vehicle = vehicle,
                            onClick = {
                                viewModel.onAction(HomeAction.SearchActiveChanged(false))
                                viewModel.onAction(HomeAction.VehicleClicked(vehicle.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    listState: LazyListState,
    contentPadding: PaddingValues,
    onVehicleClick: (Long) -> Unit,
    onDeleteVehicle: (Vehicle) -> Unit,
    onDepartmentFilterSelected: (String?) -> Unit,
    onDepartmentClickFromStats: (String) -> Unit,
    onTotalCountClick: () -> Unit,
    onTodayCountClick: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 통계 섹션
        item {
            StatisticsSection(
                totalCount = state.totalVehicleCount,
                todayCount = state.todayRegisteredCount,
                departmentStats = state.departmentStats,
                modifier = Modifier.padding(horizontal = 16.dp),
                onDepartmentClick = onDepartmentClickFromStats,
                onTotalCountClick = onTotalCountClick,
                onTodayCountClick = onTodayCountClick
            )
        }

        // 최근 등록 차량
        if (state.recentVehicles.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "최근 등록 차량",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "전체보기",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            item {
                // LazyRow는 contentPadding으로 처리해야 끝까지 스크롤해도 여백 유지
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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

        // 전체 차량 목록 헤더
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "차량 목록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "총 ${state.vehicles.size}대",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 부서 필터 칩
        if (state.departmentList.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedDepartment == null,
                            onClick = { onDepartmentFilterSelected(null) },
                            label = { Text("전체") },
                            shape = CircleShape
                        )
                    }
                    items(state.departmentList) { department ->
                        FilterChip(
                            selected = state.selectedDepartment == department,
                            onClick = { onDepartmentFilterSelected(department) },
                            label = { Text(department) },
                            shape = CircleShape
                        )
                    }
                }
            }
        }

        items(items = state.vehicles, key = { it.id }) { vehicle ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart }
            )

            LaunchedEffect(dismissState.currentValue) {
                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                    onDeleteVehicle(vehicle)
                }
            }

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                backgroundContent = {
                    val fraction = dismissState.progress.coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(VnDanger.copy(alpha = (fraction * 2f).coerceIn(0f, 1f))),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제",
                            tint = Color.White.copy(alpha = (fraction * 3f).coerceIn(0f, 1f)),
                            modifier = Modifier.padding(end = 24.dp),
                        )
                    }
                },
                modifier = Modifier.animateItem(),
            ) {
                VehicleListItem(
                    vehicle = vehicle,
                    onClick = { onVehicleClick(vehicle.id) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeContent(
            state = HomeState(
                vehicles = listOf(
                    Vehicle(
                        id = 1, licensePlate = "12가1234", ownerName = "홍길동", department = "총무부",
                        phoneNumber = "010-1234-5678", carModel = "그랜저", memo = "VIP 차량"
                    ),
                    Vehicle(
                        id = 2, licensePlate = "34나5678", ownerName = "김철수", department = "인사부",
                        carModel = "소나타"
                    ),
                    Vehicle(
                        id = 3, licensePlate = "56다9012", ownerName = "이영희", department = "개발부",
                        phoneNumber = "010-9999-9999", memo = "자주 방문"
                    )
                ),
                recentVehicles = listOf(
                    Vehicle(
                        id = 1,
                        licensePlate = "12가1234",
                        ownerName = "홍길동",
                        department = "총무부"
                    ),
                    Vehicle(id = 2, licensePlate = "34나5678", ownerName = "김철수", department = "인사부")
                ),
                totalVehicleCount = 45,
                todayRegisteredCount = 3,
                departmentStats = mapOf("총무부" to 12, "인사부" to 8, "개발부" to 15, "영업부" to 10),
                departmentList = listOf("개발부", "영업부", "인사부", "총무부")
            ),
            listState = rememberLazyListState(),
            contentPadding = PaddingValues(16.dp),
            onVehicleClick = {},
            onDeleteVehicle = {},
            onDepartmentFilterSelected = {},
            onDepartmentClickFromStats = {},
            onTotalCountClick = {},
            onTodayCountClick = {}
        )
    }
}

@Preview(showBackground = true, name = "빈 상태")
@Composable
private fun HomeScreenEmptyPreview() {
    AppTheme {
        HomeContent(
            state = HomeState(),
            listState = rememberLazyListState(),
            contentPadding = PaddingValues(16.dp),
            onVehicleClick = {},
            onDeleteVehicle = {},
            onDepartmentFilterSelected = {},
            onDepartmentClickFromStats = {},
            onTotalCountClick = {},
            onTodayCountClick = {}
        )
    }
}

@Preview(showBackground = true, name = "로딩 상태")
@Composable
private fun HomeScreenLoadingPreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
