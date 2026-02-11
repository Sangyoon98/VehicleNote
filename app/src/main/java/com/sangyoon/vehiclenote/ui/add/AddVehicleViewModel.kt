package com.sangyoon.vehiclenote.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.usecase.AddVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val addVehicleUseCase: AddVehicleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddVehicleState())
    val state: StateFlow<AddVehicleState> = _state.asStateFlow()

    fun onIntent(intent: AddVehicleIntent) {
        when (intent) {
            is AddVehicleIntent.LicensePlateChanged -> {
                _state.update {
                    it.copy(
                        licensePlate = intent.value,
                        licensePlateError = null
                    )
                }
            }
            is AddVehicleIntent.OwnerNameChanged -> {
                _state.update {
                    it.copy(
                        ownerName = intent.value,
                        ownerNameError = null
                    )
                }
            }
            is AddVehicleIntent.DepartmentChanged -> {
                _state.update {
                    it.copy(
                        department = intent.value,
                        departmentError = null
                    )
                }
            }
            is AddVehicleIntent.PhoneNumberChanged -> {
                _state.update {
                    it.copy(
                        phoneNumber = intent.value
                    )
                }
            }
            is AddVehicleIntent.CarModelChanged -> {
                _state.update {
                    it.copy(
                        carModel = intent.value
                    )
                }
            }
            is AddVehicleIntent.MemoChanged -> {
                _state.update {
                    it.copy(
                        memo = intent.value
                    )
                }
            }
            AddVehicleIntent.SaveClicked -> {
                saveVehicle()
            }
        }
    }

    private fun saveVehicle() {
        // 유효성 검사
        val validationErrors = validateInput()
        if (validationErrors) return

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val vehicle = Vehicle(
                id = 0,
                licensePlate = _state.value.licensePlate.trim(),
                ownerName = _state.value.ownerName.trim(),
                department = _state.value.department.trim(),
                phoneNumber = _state.value.phoneNumber.trim().ifBlank { null },
                carModel = _state.value.carModel.trim().ifBlank { null },
                memo = _state.value.memo.trim().ifBlank { null }
            )

            val result = addVehicleUseCase(vehicle)

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
                            error = error.message ?: "저장 실패"
                        )
                    }
                }
            )
        }
    }

    private fun validateInput(): Boolean {
        var hasError = false

        if (_state.value.licensePlate.isBlank()) {
            _state.update { it.copy(licensePlateError = "차량번호를 입력해주세요.")}
            hasError = true
        }

        if (_state.value.ownerName.isBlank()) {
            _state.update { it.copy(ownerNameError = "차주명을 입력해주세요.") }
            hasError = true
        }

        if (_state.value.department.isBlank()) {
            _state.update { it.copy(departmentError = "소속부서를 입력해주세요.") }
            hasError = true
        }

        return hasError
    }
}