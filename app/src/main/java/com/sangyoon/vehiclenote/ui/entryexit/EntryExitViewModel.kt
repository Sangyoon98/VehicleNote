package com.sangyoon.vehiclenote.ui.entryexit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.usecase.GetEntryExitRecordsUseCase
import com.sangyoon.vehiclenote.domain.usecase.RecordEntryExitUseCase
import com.sangyoon.vehiclenote.util.AnalyticsLogger
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

/**
 * 입출차 기록 화면 ViewModel (MVI 패턴).
 *
 * OCR로 인식된 번호판 또는 수동 입력 번호판에 대해 입출차 기록을 저장한다.
 * 저장 시 [RecordEntryExitUseCase]가 마지막 상태를 보고 입차/출차를 자동 토글한다.
 * 전체 기록 목록을 실시간 Flow로 구독해 UI를 자동 갱신한다.
 *
 * 상태: [EntryExitState], 사이드이펙트: [EntryExitSideEffect]
 */
@HiltViewModel
class EntryExitViewModel @Inject constructor(
    private val getRecordsUseCase: GetEntryExitRecordsUseCase,
    private val recordEntryExitUseCase: RecordEntryExitUseCase,
    private val analyticsLogger: AnalyticsLogger
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
                val typeKey = if (record.type.name == "ENTRY") "entry" else "exit"
                analyticsLogger.entryExitRecorded(record.licensePlate, typeKey)
                val typeLabel = if (typeKey == "entry") "입차" else "출차"
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
