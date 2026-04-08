package com.sangyoon.vehiclenote.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.usecase.DeleteVehicleUseCase
import com.sangyoon.vehiclenote.domain.usecase.GetAllVehiclesUseCase
import com.sangyoon.vehiclenote.domain.usecase.SearchVehicleUseCase
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllVehiclesUseCase: GetAllVehiclesUseCase,
    private val searchVehicleUseCase: SearchVehicleUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _sideEffect = Channel<HomeSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var cachedAllVehicles: List<Vehicle> = emptyList()

    init {
        loadVehicles()
    }

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
