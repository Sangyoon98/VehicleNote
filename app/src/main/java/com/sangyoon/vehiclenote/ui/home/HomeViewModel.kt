package com.sangyoon.vehiclenote.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.usecase.DeleteVehicleUseCase
import com.sangyoon.vehiclenote.domain.usecase.GetAllVehiclesUseCase
import com.sangyoon.vehiclenote.domain.usecase.SearchVehicleUseCase
import com.sangyoon.vehiclenote.util.AnalyticsLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * 홈 화면 ViewModel (MVI 패턴).
 *
 * 차량 목록 조회·검색·삭제와 통계 계산(총 차량 수, 오늘 등록 수, 부서별 현황)을 담당한다.
 * 부서 필터 선택 시 [cachedAllVehicles]에서 로컬 필터링하여 별도 DB 쿼리를 방지한다.
 *
 * 상태: [HomeState], 사이드이펙트: [HomeSideEffect]
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllVehiclesUseCase: GetAllVehiclesUseCase,
    private val searchVehicleUseCase: SearchVehicleUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase,
    private val analyticsLogger: AnalyticsLogger
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _sideEffect = Channel<HomeSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    /** 부서 필터링에 사용되는 전체 차량 캐시. DB Flow 갱신 시 함께 갱신된다. */
    private var cachedAllVehicles: List<Vehicle> = emptyList()

    init {
        loadVehicles()
    }

    /**
     * UI 액션을 처리한다. 상태 업데이트는 [HomeState.reduce]에 위임하고,
     * 부수 작업(검색, 삭제, 네비게이션)만 직접 수행한다.
     */
    fun onAction(action: HomeAction) {
        _state.update { it.reduce(action) }

        when (action) {
            is HomeAction.SearchQueryChanged ->
                if (action.query.isNotBlank()) searchVehicles(action.query) else loadVehicles()

            is HomeAction.SearchActiveChanged ->
                if (!action.isActive) loadVehicles()

            is HomeAction.DeleteVehicle -> deleteVehicle(action.vehicle)
            is HomeAction.AddVehicleClicked -> sendSideEffect(HomeSideEffect.NavigateToAdd)
            is HomeAction.VehicleClicked -> sendSideEffect(HomeSideEffect.NavigateToDetail(action.vehicleId))
            is HomeAction.Refresh -> loadVehicles()
            is HomeAction.DepartmentFilterSelected -> applyDepartmentFilter(action.department)
        }
    }

    /**
     * 통계 대시보드에서 부서 항목을 탭했을 때 호출된다.
     *
     * 해당 부서로 필터를 적용하고, 필터 칩 영역으로 스크롤하는 사이드이펙트를 발생시킨다.
     *
     * @param department 선택한 부서명.
     */
    fun selectDepartmentFromStats(department: String) {
        _state.update { it.copy(selectedDepartment = department) }
        applyDepartmentFilter(department)
        sendSideEffect(HomeSideEffect.ScrollToFilter)
    }

    private fun applyDepartmentFilter(department: String?) {
        val filtered = if (department == null) cachedAllVehicles
        else cachedAllVehicles.filter { it.department == department }
        _state.update { it.copy(vehicles = filtered) }
    }

    private fun loadVehicles() {
        _state.update { it.copy(isLoading = true) }
        getAllVehiclesUseCase()
            .onEach { vehicles ->
                cachedAllVehicles = vehicles
                _state.update { it.withComputedStats(vehicles) }
            }
            .catch { error -> _state.update { it.copy(isLoading = false, error = error.message) } }
            .launchIn(viewModelScope)
    }

    private fun searchVehicles(query: String) {
        searchVehicleUseCase(query)
            .onEach { vehicles ->
                _state.update { it.copy(vehicles = vehicles, isLoading = false, error = null) }
            }
            .catch { error -> _state.update { it.copy(isLoading = false, error = error.message) } }
            .launchIn(viewModelScope)
    }

    private fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            deleteVehicleUseCase(vehicle).fold(
                onSuccess = {
                    analyticsLogger.vehicleDeleted(vehicle.licensePlate)
                    sendSideEffect(HomeSideEffect.ShowSnackbar("${vehicle.licensePlate} 차량이 삭제되었습니다"))
                },
                onFailure = {
                    sendSideEffect(HomeSideEffect.ShowSnackbar("차량 삭제에 실패하였습니다"))
                }
            )
        }
    }

    private fun sendSideEffect(effect: HomeSideEffect) {
        viewModelScope.launch { _sideEffect.send(effect) }
    }

    private fun HomeState.withComputedStats(vehicles: List<Vehicle>): HomeState {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val deptStats = vehicles
            .filter { it.department != null }
            .groupBy { it.department!! }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .associate { it.key to it.value }

        val deptList = vehicles
            .mapNotNull { it.department }
            .distinct()
            .sorted()

        val filteredVehicles = if (selectedDepartment == null) vehicles
        else vehicles.filter { it.department == selectedDepartment }

        return copy(
            vehicles = filteredVehicles,
            recentVehicles = vehicles.take(5),
            totalVehicleCount = vehicles.size,
            todayRegisteredCount = vehicles.count { it.createdAt >= todayStart },
            departmentStats = deptStats,
            departmentList = deptList,
            isLoading = false,
            error = null
        )
    }
}
