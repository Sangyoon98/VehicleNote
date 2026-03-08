package com.sangyoon.vehiclenote.ui.entryexitdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.usecase.GetEntryExitRecordByIdUseCase
import com.sangyoon.vehiclenote.domain.usecase.GetVehicleByLicensePlateUseCase
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
class EntryExitDetailViewModel @Inject constructor(
    private val getRecordByIdUseCase: GetEntryExitRecordByIdUseCase,
    private val getVehicleByPlateUseCase: GetVehicleByLicensePlateUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recordId: Long = checkNotNull(savedStateHandle["recordId"])

    private val _state = MutableStateFlow(EntryExitDetailState())
    val state: StateFlow<EntryExitDetailState> = _state.asStateFlow()

    private val _sideEffect = Channel<EntryExitDetailSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadRecord()
    }

    fun onAction(action: EntryExitDetailAction) {
        when (action) {
            EntryExitDetailAction.NavigateBackClicked -> {
                sendSideEffect(EntryExitDetailSideEffect.NavigateBack)
            }
            EntryExitDetailAction.RegisterVehicleClicked -> {
                val plate = _state.value.record?.licensePlate ?: return
                sendSideEffect(EntryExitDetailSideEffect.NavigateToAddVehicle(plate))
            }
        }
    }

    private fun loadRecord() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val record = getRecordByIdUseCase(recordId)
            if (record == null) {
                _state.update { it.copy(isLoading = false, error = "기록을 찾을 수 없습니다.") }
                return@launch
            }
            val vehicle = getVehicleByPlateUseCase(record.licensePlate)
            _state.update {
                it.copy(
                    isLoading = false,
                    record = record,
                    vehicle = vehicle
                )
            }
        }
    }

    private fun sendSideEffect(effect: EntryExitDetailSideEffect) {
        viewModelScope.launch { _sideEffect.send(effect) }
    }
}
