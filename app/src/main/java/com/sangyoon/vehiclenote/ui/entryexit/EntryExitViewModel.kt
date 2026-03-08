package com.sangyoon.vehiclenote.ui.entryexit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.usecase.GetEntryExitRecordsUseCase
import com.sangyoon.vehiclenote.domain.usecase.RecordEntryExitUseCase
import com.sangyoon.vehiclenote.domain.usecase.SearchEntryExitRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val searchRecordsUseCase: SearchEntryExitRecordsUseCase,
    private val recordEntryExitUseCase: RecordEntryExitUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EntryExitState())
    val state: StateFlow<EntryExitState> = _state.asStateFlow()

    private val _sideEffect = Channel<EntryExitSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var recordsJob: Job? = null

    init {
        loadAllRecords()
    }

    fun onAction(action: EntryExitAction) {
        when (action) {
            EntryExitAction.SearchToggled -> {
                _state.update { it.copy(isSearching = true, searchQuery = "") }
            }
            is EntryExitAction.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = action.query) }
                loadRecords(action.query)
            }
            EntryExitAction.SearchDismissed -> {
                _state.update { it.copy(isSearching = false, searchQuery = "") }
                loadAllRecords()
            }

            is EntryExitAction.PlateDetected -> {
                if (!_state.value.showPlateConfirmDialog) {
                    _state.update { it.copy(showPlateConfirmDialog = true, detectedPlate = action.plate) }
                }
            }
            is EntryExitAction.DetectedPlateEdited -> {
                _state.update { it.copy(detectedPlate = action.plate) }
            }
            EntryExitAction.PlateConfirmed -> {
                val plate = _state.value.detectedPlate.trim()
                _state.update { it.copy(showPlateConfirmDialog = false, detectedPlate = "") }
                if (plate.isNotBlank()) recordEntryExit(plate)
            }
            EntryExitAction.PlateConfirmDismissed -> {
                _state.update { it.copy(showPlateConfirmDialog = false, detectedPlate = "") }
            }

            EntryExitAction.ManualInputClicked -> {
                _state.update { it.copy(showManualInputDialog = true, manualInputPlate = "") }
            }
            is EntryExitAction.ManualPlateChanged -> {
                _state.update { it.copy(manualInputPlate = action.plate) }
            }
            EntryExitAction.ManualInputConfirmed -> {
                val plate = _state.value.manualInputPlate.trim()
                _state.update { it.copy(showManualInputDialog = false, manualInputPlate = "") }
                if (plate.isNotBlank()) recordEntryExit(plate)
            }
            EntryExitAction.ManualInputDismissed -> {
                _state.update { it.copy(showManualInputDialog = false, manualInputPlate = "") }
            }

            is EntryExitAction.RecordClicked -> {
                sendSideEffect(EntryExitSideEffect.NavigateToDetail(action.recordId))
            }
        }
    }

    private fun loadAllRecords() {
        loadRecords("")
    }

    private fun loadRecords(query: String) {
        recordsJob?.cancel()
        recordsJob = viewModelScope.launch {
            val flow = if (query.isBlank()) {
                getRecordsUseCase()
            } else {
                searchRecordsUseCase(query)
            }
            flow.catch { e ->
                _state.update { it.copy(error = e.message) }
            }.collect { records ->
                _state.update { it.copy(records = records, isLoading = false) }
            }
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
