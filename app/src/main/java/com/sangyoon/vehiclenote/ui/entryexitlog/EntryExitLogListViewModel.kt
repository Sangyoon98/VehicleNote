package com.sangyoon.vehiclenote.ui.entryexitlog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.usecase.GetEntryExitRecordsUseCase
import com.sangyoon.vehiclenote.domain.usecase.SearchEntryExitRecordsUseCase
import com.sangyoon.vehiclenote.util.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 입출차 기록 전체 목록 화면 ViewModel (MVI 패턴).
 *
 * 네비게이션 인수 `filterToday`가 true이면 오늘 기록만 표시한다.
 * 검색어가 바뀔 때마다 기존 Flow 구독을 취소하고 새 쿼리로 재구독한다.
 * 현재 표시 중인 기록을 CSV로 내보내는 기능도 제공한다.
 *
 * 상태: [EntryExitLogListState], 사이드이펙트: [EntryExitLogListSideEffect]
 */
@HiltViewModel
class EntryExitLogListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRecordsUseCase: GetEntryExitRecordsUseCase,
    private val searchRecordsUseCase: SearchEntryExitRecordsUseCase,
    private val csvExporter: CsvExporter,
) : ViewModel() {

    private val filterToday: Boolean = savedStateHandle.get<Boolean>("filterToday") ?: false

    private val _state = MutableStateFlow(EntryExitLogListState(isFilteredToday = filterToday))
    val state: StateFlow<EntryExitLogListState> = _state.asStateFlow()

    private val _sideEffect = Channel<EntryExitLogListSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var recordsJob: Job? = null

    init {
        loadRecords("")
    }

    fun onAction(action: EntryExitLogListAction) {
        when (action) {
            is EntryExitLogListAction.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = action.query) }
                loadRecords(action.query)
            }

            is EntryExitLogListAction.SearchActiveChanged -> {
                _state.update { it.copy(isSearchActive = action.active) }
                if (!action.active) {
                    _state.update { it.copy(searchQuery = "") }
                    loadRecords("")
                }
            }

            is EntryExitLogListAction.RecordClicked -> {
                viewModelScope.launch {
                    _sideEffect.send(EntryExitLogListSideEffect.NavigateToDetail(action.recordId))
                }
            }

            EntryExitLogListAction.OnExportClicked -> {
                viewModelScope.launch {
                    _state.update { it.copy(isExporting = true) }
                    runCatching {
                        withContext(Dispatchers.IO) {
                            csvExporter.export(_state.value.records)
                        }
                    }.onSuccess { uri ->
                        val fileName = "entry_exit_${fileTimestamp()}.csv"
                        _sideEffect.send(EntryExitLogListSideEffect.ShareFile(uri, fileName))
                    }.also {
                        _state.update { it.copy(isExporting = false) }
                    }
                }
            }
        }
    }

    private fun fileTimestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    private fun loadRecords(query: String) {
        recordsJob?.cancel()
        recordsJob = viewModelScope.launch {
            val flow = if (query.isBlank()) getRecordsUseCase() else searchRecordsUseCase(query)
            flow.catch {
                _state.update { it.copy(isLoading = false) }
            }.collect { records ->
                val filtered = if (filterToday) {
                    val todayStart = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    records.filter { it.timestamp >= todayStart }
                } else records
                _state.update { it.copy(records = filtered, isLoading = false) }
            }
        }
    }
}
