package com.sangyoon.vehiclenote.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.usecase.AddVehicleUseCase
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
class AddVehicleViewModel @Inject constructor(
    private val addVehicleUseCase: AddVehicleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddVehicleState())
    val state: StateFlow<AddVehicleState> = _state.asStateFlow()

    private val _sideEffect = Channel<AddVehicleSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onAction(action: AddVehicleAction) {
        when (action) {
            is AddVehicleAction.LicensePlateChanged ->
                _state.update { it.copy(licensePlate = action.value, licensePlateError = null) }
            is AddVehicleAction.OwnerNameChanged ->
                _state.update { it.copy(ownerName = action.value, ownerNameError = null) }
            is AddVehicleAction.DepartmentChanged ->
                _state.update { it.copy(department = action.value, departmentError = null) }
            is AddVehicleAction.PhoneNumberChanged ->
                _state.update { it.copy(phoneNumber = action.value) }
            is AddVehicleAction.CarModelChanged ->
                _state.update { it.copy(carModel = action.value) }
            is AddVehicleAction.MemoChanged ->
                _state.update { it.copy(memo = action.value) }
            AddVehicleAction.SaveClicked -> saveVehicle()
        }
    }

    private fun saveVehicle() {
        if (validateInput()) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val current = _state.value
            val vehicle = Vehicle(
                licensePlate = current.licensePlate.trim(),
                ownerName = current.ownerName.trim(),
                department = current.department.trim(),
                phoneNumber = current.phoneNumber.trim().ifBlank { null },
                carModel = current.carModel.trim().ifBlank { null },
                memo = current.memo.trim().ifBlank { null }
            )

            addVehicleUseCase(vehicle).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }
                    _sideEffect.send(AddVehicleSideEffect.NavigateBack)
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, error = error.message ?: "저장 실패") }
                }
            )
        }
    }

    private fun validateInput(): Boolean {
        var hasError = false
        val current = _state.value

        if (current.licensePlate.isBlank()) {
            _state.update { it.copy(licensePlateError = "차량번호를 입력해주세요.") }
            hasError = true
        }
        if (current.ownerName.isBlank()) {
            _state.update { it.copy(ownerNameError = "차주명을 입력해주세요.") }
            hasError = true
        }
        if (current.department.isBlank()) {
            _state.update { it.copy(departmentError = "소속부서를 입력해주세요.") }
            hasError = true
        }

        return hasError
    }
}
