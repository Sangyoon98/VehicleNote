package com.sangyoon.vehiclenote.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.usecase.GetVehicleByIdUseCase
import com.sangyoon.vehiclenote.domain.usecase.UpdateVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EditVehicleViewModel @Inject constructor(
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val updateVehicleUseCase: UpdateVehicleUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = checkNotNull(savedStateHandle["vehicleId"])

    private val _state = MutableStateFlow(EditVehicleState())
    val state: StateFlow<EditVehicleState> = _state.asStateFlow()

    init {
        loadVehicle()
    }

    fun onIntent(intent: EditVehicleIntent) {
        when (intent) {
            is EditVehicleIntent.LicensePlateChanged -> {
                _state.update {
                    it.copy(
                        licensePlate = intent.value,
                        licensePlateError = null
                    )
                }
            }
            is EditVehicleIntent.OwnerNameChanged -> {
                _state.update {
                    it.copy(
                        ownerName = intent.value,
                        ownerNameError = null
                    )
                }
            }
            is EditVehicleIntent.DepartmentChanged -> {
                _state.update {
                    it.copy(
                        department = intent.value,
                        departmentError = null
                    )
                }
            }
            is EditVehicleIntent.PhoneNumberChanged -> {
                _state.update { it.copy(phoneNumber = intent.value) }
            }
            is EditVehicleIntent.CarModelChanged -> {
                _state.update { it.copy(carModel = intent.value) }
            }
            is EditVehicleIntent.MemoChanged -> {
                _state.update { it.copy(memo = intent.value) }
            }
            is EditVehicleIntent.SaveClicked -> {
                updateVehicle()
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

    private fun updateVehicle() {
        // 유효성 검사
        val validationErrors = validateInput()
        if (validationErrors) return

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val vehicle = Vehicle(
                id = _state.value.vehicleId,
                licensePlate = _state.value.licensePlate.trim(),
                ownerName = _state.value.ownerName.trim(),
                department = _state.value.department.trim(),
                phoneNumber = _state.value.phoneNumber.trim().ifBlank { null },
                carModel = _state.value.carModel.trim().ifBlank { null },
                memo = _state.value.memo.trim().ifBlank { null }
            )

            val result = updateVehicleUseCase(vehicle)

            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSaved = true,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "수정 실패"
                        )
                    }
                }
            )
        }
    }

    private fun validateInput(): Boolean {
        var hasError = false

        if (_state.value.licensePlate.isBlank()) {
            _state.update { it.copy(licensePlateError = "차량번호를 입력해주세요") }
            hasError = true
        }

        if (_state.value.ownerName.isBlank()) {
            _state.update { it.copy(ownerNameError = "차주명을 입력해주세요") }
            hasError = true
        }

        if (_state.value.department.isBlank()) {
            _state.update { it.copy(departmentError = "소속부서를 입력해주세요") }
            hasError = true
        }

        return hasError
    }
}