package com.sangyoon.vehiclenote.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.usecase.GetVehicleByIdUseCase
import com.sangyoon.vehiclenote.domain.usecase.UpdateVehicleUseCase
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
class EditVehicleViewModel @Inject constructor(
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val updateVehicleUseCase: UpdateVehicleUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = checkNotNull(savedStateHandle["vehicleId"])

    private val _state = MutableStateFlow(EditVehicleState())
    val state: StateFlow<EditVehicleState> = _state.asStateFlow()

    private val _sideEffect = Channel<EditVehicleSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadVehicle()
    }

    fun onAction(action: EditVehicleAction) {
        when (action) {
            is EditVehicleAction.LicensePlateChanged ->
                _state.update { it.copy(licensePlate = action.value, licensePlateError = null) }
            is EditVehicleAction.OwnerNameChanged ->
                _state.update { it.copy(ownerName = action.value, ownerNameError = null) }
            is EditVehicleAction.DepartmentChanged ->
                _state.update { it.copy(department = action.value, departmentError = null) }
            is EditVehicleAction.PhoneNumberChanged ->
                _state.update { it.copy(phoneNumber = action.value) }
            is EditVehicleAction.CarModelChanged ->
                _state.update { it.copy(carModel = action.value) }
            is EditVehicleAction.MemoChanged ->
                _state.update { it.copy(memo = action.value) }
            EditVehicleAction.SaveClicked -> updateVehicle()
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
                            vehicleId = vehicle.id,
                            licensePlate = vehicle.licensePlate,
                            ownerName = vehicle.ownerName,
                            department = vehicle.department,
                            phoneNumber = vehicle.phoneNumber ?: "",
                            carModel = vehicle.carModel ?: "",
                            memo = vehicle.memo ?: "",
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "차량을 찾을 수 없습니다") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "오류 발생") }
            }
        }
    }

    private fun updateVehicle() {
        if (validateInput()) return

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val current = _state.value
            val vehicle = Vehicle(
                id = current.vehicleId,
                licensePlate = current.licensePlate.trim(),
                ownerName = current.ownerName.trim(),
                department = current.department.trim(),
                phoneNumber = current.phoneNumber.trim().ifBlank { null },
                carModel = current.carModel.trim().ifBlank { null },
                memo = current.memo.trim().ifBlank { null }
            )

            updateVehicleUseCase(vehicle).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }
                    _sideEffect.send(EditVehicleSideEffect.NavigateBack)
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, error = error.message ?: "수정 실패") }
                }
            )
        }
    }

    private fun validateInput(): Boolean {
        var hasError = false
        val current = _state.value

        if (current.licensePlate.isBlank()) {
            _state.update { it.copy(licensePlateError = "차량번호를 입력해주세요") }
            hasError = true
        }
        if (current.ownerName.isBlank()) {
            _state.update { it.copy(ownerNameError = "차주명을 입력해주세요") }
            hasError = true
        }
        if (current.department.isBlank()) {
            _state.update { it.copy(departmentError = "소속부서를 입력해주세요") }
            hasError = true
        }

        return hasError
    }
}
