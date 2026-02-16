package com.sangyoon.vehiclenote.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.usecase.DeleteVehicleUseCase
import com.sangyoon.vehiclenote.domain.usecase.GetVehicleByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = checkNotNull(savedStateHandle["vehicleId"])

    private val _state = MutableStateFlow(VehicleDetailState())
    val state: StateFlow<VehicleDetailState> = _state.asStateFlow()

    init {
        loadVehicle()
    }

    fun onIntent(intent: VehicleDetailIntent) {
        when (intent) {
            is VehicleDetailIntent.DeleteClicked -> {
                deleteVehicle()
            }
            is VehicleDetailIntent.EditClicked -> {

            }
            is VehicleDetailIntent.MessageShown -> {
                _state.update { it.copy(userMessage = null) }
            }
        }
    }

    private fun loadVehicle() {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val vehicle = getVehicleByIdUseCase(vehicleId)

                if (vehicle != null) {
                    _state.update {
                        it.copy(
                            vehicle = vehicle,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "차량을 찾을 수 없습니다"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "오류 발생"
                    )
                }
            }
        }
    }

    private fun deleteVehicle() {
        val vehicle = _state.value.vehicle ?: return

        viewModelScope.launch {
            deleteVehicleUseCase(vehicle).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isDeleted = true,
                            userMessage = "${vehicle.licensePlate} 차량이 삭제되었습니다"
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(userMessage = "삭제 실패: ${error.message}")
                    }
                }
            )
        }
    }
}