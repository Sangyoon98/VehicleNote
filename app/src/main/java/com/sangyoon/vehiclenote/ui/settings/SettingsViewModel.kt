package com.sangyoon.vehiclenote.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import com.sangyoon.vehiclenote.domain.usecase.AddVehicleUseCase
import com.sangyoon.vehiclenote.domain.usecase.GetAllVehiclesUseCase
import com.sangyoon.vehiclenote.domain.usecase.GetRetentionPeriodUseCase
import com.sangyoon.vehiclenote.domain.usecase.GetVehicleByLicensePlateUseCase
import com.sangyoon.vehiclenote.domain.usecase.PurgeOldRecordsUseCase
import com.sangyoon.vehiclenote.domain.usecase.SetRetentionPeriodUseCase
import com.sangyoon.vehiclenote.util.VehicleCsvExporter
import com.sangyoon.vehiclenote.util.VehicleCsvParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 설정 화면 ViewModel (MVI 패턴).
 *
 * 다음 기능을 담당한다:
 * - 입출차 기록 보관 기간 조회·변경 (DataStore 연동)
 * - 차량 데이터 CSV 내보내기 ([VehicleCsvExporter])
 * - 차량 데이터 CSV 가져오기 ([VehicleCsvParser]) — 번호판 중복 시 건너뜀
 * - [DataRetentionPeriod.UNLIMITED] 선택 시 경고 다이얼로그 표시 후 적용
 *
 * 상태: [SettingsState], 사이드이펙트: [SettingsSideEffect]
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getRetentionPeriod: GetRetentionPeriodUseCase,
    private val setRetentionPeriod: SetRetentionPeriodUseCase,
    private val purgeOldRecords: PurgeOldRecordsUseCase,
    private val getAllVehicles: GetAllVehiclesUseCase,
    private val addVehicle: AddVehicleUseCase,
    private val getVehicleByLicensePlate: GetVehicleByLicensePlateUseCase,
    private val vehicleCsvExporter: VehicleCsvExporter,
    private val vehicleCsvParser: VehicleCsvParser,
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
            // ── 저장 기간 ──────────────────────────────────────────────────────────
            SettingsAction.OnRetentionPeriodClicked ->
                _state.update { it.copy(showRetentionDialog = true) }

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
                    applyRetentionPeriod(action.period)
                    _state.update { it.copy(showRetentionDialog = false) }
                }
            }

            SettingsAction.OnRetentionDialogDismissed ->
                _state.update { it.copy(showRetentionDialog = false) }

            SettingsAction.OnUnlimitedWarningConfirmed -> {
                val pending = _state.value.pendingPeriod ?: return
                applyRetentionPeriod(pending)
                _state.update { it.copy(showUnlimitedWarningDialog = false, pendingPeriod = null) }
            }

            SettingsAction.OnUnlimitedWarningDismissed ->
                _state.update { it.copy(showUnlimitedWarningDialog = false, pendingPeriod = null) }

            // ── 차량 데이터 내보내기 ─────────────────────────────────────────────
            SettingsAction.OnExportVehiclesClicked -> {
                viewModelScope.launch {
                    _state.update { it.copy(isExportingVehicles = true) }
                    runCatching {
                        val vehicles = withContext(Dispatchers.IO) { getAllVehicles().first() }
                        withContext(Dispatchers.IO) { vehicleCsvExporter.export(vehicles) }
                    }.onSuccess { uri ->
                        val fileName = "vehicles_${fileTimestamp()}.csv"
                        _sideEffect.send(SettingsSideEffect.ShareVehicleCsv(uri, fileName))
                    }.also {
                        _state.update { it.copy(isExportingVehicles = false) }
                    }
                }
            }

            // ── 차량 데이터 가져오기 ─────────────────────────────────────────────
            SettingsAction.OnImportVehiclesClicked ->
                viewModelScope.launch { _sideEffect.send(SettingsSideEffect.LaunchVehicleFilePicker) }

            is SettingsAction.OnImportFilePicked -> {
                viewModelScope.launch {
                    _state.update { it.copy(isImportingVehicles = true) }
                    runCatching {
                        withContext(Dispatchers.IO) { vehicleCsvParser.parse(action.uri) }
                    }.onSuccess { result ->
                        _state.update {
                            it.copy(
                                pendingImportVehicles = result.vehicles,
                                pendingSkippedRows = result.skippedRows,
                                showImportConfirmDialog = true
                            )
                        }
                    }.also {
                        _state.update { it.copy(isImportingVehicles = false) }
                    }
                }
            }

            SettingsAction.OnImportConfirmed -> {
                val pending = _state.value.pendingImportVehicles ?: return
                _state.update { it.copy(showImportConfirmDialog = false, isImportingVehicles = true) }
                viewModelScope.launch {
                    var added = 0
                    var skippedDuplicate = 0
                    withContext(Dispatchers.IO) {
                        pending.forEach { vehicle ->
                            val exists = getVehicleByLicensePlate(vehicle.licensePlate) != null
                            if (exists) {
                                skippedDuplicate++
                            } else {
                                addVehicle(vehicle)
                                added++
                            }
                        }
                    }
                    _state.update {
                        it.copy(
                            isImportingVehicles = false,
                            pendingImportVehicles = null,
                            pendingSkippedRows = 0,
                            importResult = VehicleImportResult(added, skippedDuplicate)
                        )
                    }
                }
            }

            SettingsAction.OnImportDialogDismissed ->
                _state.update {
                    it.copy(
                        showImportConfirmDialog = false,
                        pendingImportVehicles = null,
                        pendingSkippedRows = 0
                    )
                }

            SettingsAction.OnImportResultDismissed ->
                _state.update { it.copy(importResult = null) }
        }
    }

    private fun applyRetentionPeriod(period: DataRetentionPeriod) {
        viewModelScope.launch {
            setRetentionPeriod(period)
            purgeOldRecords()
        }
    }

    private fun fileTimestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}
