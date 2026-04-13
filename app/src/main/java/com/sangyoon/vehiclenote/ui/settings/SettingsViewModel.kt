package com.sangyoon.vehiclenote.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import com.sangyoon.vehiclenote.domain.usecase.GetRetentionPeriodUseCase
import com.sangyoon.vehiclenote.domain.usecase.PurgeOldRecordsUseCase
import com.sangyoon.vehiclenote.domain.usecase.SetRetentionPeriodUseCase
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
class SettingsViewModel @Inject constructor(
    private val getRetentionPeriod: GetRetentionPeriodUseCase,
    private val setRetentionPeriod: SetRetentionPeriodUseCase,
    private val purgeOldRecords: PurgeOldRecordsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _sideEffect = Channel<SettingsSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            getRetentionPeriod().collect { period ->
                _state.update { it.copy(retentionPeriod = period) }
            }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.OnRetentionPeriodClicked -> {
                _state.update { it.copy(showRetentionDialog = true) }
            }

            is SettingsAction.OnPeriodSelected -> {
                if (action.period == DataRetentionPeriod.UNLIMITED) {
                    _state.update {
                        it.copy(
                            showRetentionDialog = false,
                            showUnlimitedWarningDialog = true,
                            pendingPeriod = DataRetentionPeriod.UNLIMITED
                        )
                    }
                } else {
                    applyPeriod(action.period)
                    _state.update { it.copy(showRetentionDialog = false) }
                }
            }

            SettingsAction.OnRetentionDialogDismissed -> {
                _state.update { it.copy(showRetentionDialog = false) }
            }

            SettingsAction.OnUnlimitedWarningConfirmed -> {
                val pending = _state.value.pendingPeriod ?: return
                applyPeriod(pending)
                _state.update {
                    it.copy(
                        showUnlimitedWarningDialog = false,
                        pendingPeriod = null
                    )
                }
            }

            SettingsAction.OnUnlimitedWarningDismissed -> {
                _state.update {
                    it.copy(
                        showUnlimitedWarningDialog = false,
                        pendingPeriod = null
                    )
                }
            }
        }
    }

    private fun applyPeriod(period: DataRetentionPeriod) {
        viewModelScope.launch {
            setRetentionPeriod(period)
            purgeOldRecords()
        }
    }
}
