package com.sangyoon.vehiclenote.ui.entryexitlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.usecase.GetEntryExitRecordsUseCase
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
class EntryExitLogListViewModel @Inject constructor(
    private val getRecordsUseCase: GetEntryExitRecordsUseCase,
    private val searchRecordsUseCase: SearchEntryExitRecordsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(EntryExitLogListState())
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
        }
    }

    private fun loadRecords(query: String) {
        recordsJob?.cancel()
        recordsJob = viewModelScope.launch {
            val flow = if (query.isBlank()) getRecordsUseCase() else searchRecordsUseCase(query)
            flow.catch { e ->
                _state.update { it.copy(isLoading = false) }
            }.collect { records ->
                _state.update { it.copy(records = records, isLoading = false) }
            }
        }
    }
}
