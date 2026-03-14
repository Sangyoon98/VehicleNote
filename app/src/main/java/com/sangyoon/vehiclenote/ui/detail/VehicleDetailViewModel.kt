package com.sangyoon.vehiclenote.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.usecase.DeleteVehicleUseCase
import com.sangyoon.vehiclenote.domain.usecase.GetVehicleByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = checkNotNull(savedStateHandle["vehicleId"])

    private val _state = MutableStateFlow(VehicleDetailState())
    val state: StateFlow<VehicleDetailState> = _state.asStateFlow()

    private val _sideEffect = Channel<VehicleDetailSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadVehicle()
    }

    fun onAction(action: VehicleDetailAction) {
        when (action) {
            VehicleDetailAction.ShowDeleteDialog -> _state.update { it.copy(showDeleteDialog = true) }
            VehicleDetailAction.DismissDeleteDialog -> _state.update { it.copy(showDeleteDialog = false) }
            VehicleDetailAction.DeleteConfirmed -> {
                _state.update { it.copy(showDeleteDialog = false) }
                deleteVehicle()
            }

            VehicleDetailAction.EditClicked ->
                viewModelScope.launch {
                    _sideEffect.send(
                        VehicleDetailSideEffect.NavigateToEdit(
                            vehicleId
                        )
                    )
                }
        }
    }

    private fun loadVehicle() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val vehicle = getVehicleByIdUseCase(vehicleId)
                if (vehicle != null) {
                    _state.update { it.copy(vehicle = vehicle, isLoading = false, error = null) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "차량을 찾을 수 없습니다") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "오류 발생") }
            }
        }
    }

    private fun deleteVehicle() {
        val vehicle = _state.value.vehicle ?: return
        viewModelScope.launch {
            deleteVehicleUseCase(vehicle).fold(
                onSuccess = {
                    _sideEffect.send(VehicleDetailSideEffect.ShowSnackbar("${vehicle.licensePlate} 차량이 삭제되었습니다"))
                    _sideEffect.send(VehicleDetailSideEffect.NavigateBack)
                },
                onFailure = { error ->
                    _sideEffect.send(VehicleDetailSideEffect.ShowSnackbar("삭제 실패: ${error.message}"))
                }
            )
        }
    }
}
