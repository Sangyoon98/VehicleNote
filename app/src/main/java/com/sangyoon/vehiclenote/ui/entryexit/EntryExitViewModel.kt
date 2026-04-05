package com.sangyoon.vehiclenote.ui.entryexit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.usecase.GetEntryExitRecordsUseCase
import com.sangyoon.vehiclenote.domain.usecase.RecordEntryExitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryExitViewModel @Inject constructor(
    private val getRecordsUseCase: GetEntryExitRecordsUseCase,
    private val recordEntryExitUseCase: RecordEntryExitUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EntryExitState())
    val state: StateFlow<EntryExitState> = _state.asStateFlow()

    private val _sideEffect = Channel<EntryExitSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            getRecordsUseCase()
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { records -> _state.update { it.copy(records = records, isLoading = false) } }
        }
    }

    fun onAction(action: EntryExitAction) {
        val currentState = _state.value
        _state.update { it.reduce(action) }

        when (action) {
            EntryExitAction.PlateConfirmed -> {
                val plate = currentState.detectedPlate.trim()
                if (plate.isNotBlank()) recordEntryExit(plate)
            }

            EntryExitAction.ManualInputConfirmed -> {
                val plate = currentState.manualInputPlate.trim()
                if (plate.isNotBlank()) recordEntryExit(plate)
            }

            is EntryExitAction.RecordClicked ->
                sendSideEffect(EntryExitSideEffect.NavigateToDetail(action.recordId))

            EntryExitAction.LogListClicked ->
                sendSideEffect(EntryExitSideEffect.NavigateToLogList)

            else -> Unit
        }
    }

    private fun recordEntryExit(plate: String) {
        viewModelScope.launch {
            runCatching {
                recordEntryExitUseCase(plate)
            }.onSuccess { record ->
                val typeLabel = if (record.type.name == "ENTRY") "입차" else "출차"
                sendSideEffect(EntryExitSideEffect.ShowSnackbar("${record.licensePlate} $typeLabel 처리됨"))
            }.onFailure { e ->
                sendSideEffect(EntryExitSideEffect.ShowSnackbar("오류: ${e.message}"))
            }
        }
    }

    private fun sendSideEffect(effect: EntryExitSideEffect) {
        viewModelScope.launch { _sideEffect.send(effect) }
    }
}
