package com.sangyoon.vehiclenote.feature.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.usecase.GetAllVehiclesUseCase
import com.sangyoon.vehiclenote.domain.usecase.GetRetentionPeriodUseCase
import com.sangyoon.vehiclenote.domain.usecase.ImportVehiclesUseCase
import com.sangyoon.vehiclenote.domain.usecase.PurgeOldRecordsUseCase
import com.sangyoon.vehiclenote.domain.usecase.SetRetentionPeriodUseCase
import com.sangyoon.vehiclenote.core.analytics.AnalyticsLogger
import com.sangyoon.vehiclenote.feature.settings.VehicleCsvExporter
import com.sangyoon.vehiclenote.feature.settings.VehicleCsvParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
 * - 차량 데이터 CSV 가져오기 ([VehicleCsvParser] 파싱 → [ImportVehiclesUseCase] 등록)
 * - [DataRetentionPeriod.UNLIMITED] 선택 시 경고 다이얼로그 표시 후 적용
 *
 * 동기 상태 전이는 [SettingsState.reduce]가 담당하고,
 * ViewModel은 유스케이스 실행과 비동기 결과 반영만 처리한다.
 *
 * 상태: [SettingsState], 사이드이펙트: [SettingsSideEffect]
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getRetentionPeriod: GetRetentionPeriodUseCase,
    private val setRetentionPeriod: SetRetentionPeriodUseCase,
    private val purgeOldRecords: PurgeOldRecordsUseCase,
    private val getAllVehicles: GetAllVehiclesUseCase,
    private val importVehicles: ImportVehiclesUseCase,
    private val vehicleCsvExporter: VehicleCsvExporter,
    private val vehicleCsvParser: VehicleCsvParser,
    private val analyticsLogger: AnalyticsLogger,
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
        val currentState = _state.value
        _state.update { it.reduce(action) }

        when (action) {
            is SettingsAction.OnPeriodSelected ->
                if (action.period != DataRetentionPeriod.UNLIMITED) {
                    applyRetentionPeriod(action.period)
                }

            SettingsAction.OnUnlimitedWarningConfirmed ->
                currentState.pendingPeriod?.let { applyRetentionPeriod(it) }

            SettingsAction.OnExportVehiclesClicked -> exportVehicles()

            SettingsAction.OnImportVehiclesClicked ->
                viewModelScope.launch { _sideEffect.send(SettingsSideEffect.LaunchVehicleFilePicker) }

            is SettingsAction.OnImportFilePicked -> parseImportFile(action.uri)

            SettingsAction.OnImportConfirmed ->
                currentState.pendingImportVehicles?.let { confirmImport(it) }

            else -> Unit
        }
    }

    private fun exportVehicles() {
        viewModelScope.launch {
            runCatching {
                val vehicles = getAllVehicles().first()
                vehicles.size to vehicleCsvExporter.export(vehicles)
            }.onSuccess { (count, uri) ->
                analyticsLogger.csvExported(count)
                val fileName = "vehicles_${fileTimestamp()}.csv"
                _sideEffect.send(SettingsSideEffect.ShareVehicleCsv(uri, fileName))
            }.also {
                _state.update { it.copy(isExportingVehicles = false) }
            }
        }
    }

    private fun parseImportFile(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                vehicleCsvParser.parse(uri)
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

    private fun confirmImport(pending: List<Vehicle>) {
        viewModelScope.launch {
            runCatching {
                importVehicles(pending)
            }.onSuccess { result ->
                analyticsLogger.csvImported(result.addedCount, result.skippedByDuplicate)
                _state.update {
                    it.copy(
                        pendingImportVehicles = null,
                        pendingSkippedRows = 0,
                        importResult = VehicleImportResult(
                            addedCount = result.addedCount,
                            skippedByDuplicate = result.skippedByDuplicate
                        )
                    )
                }
            }.also {
                _state.update { it.copy(isImportingVehicles = false) }
            }
        }
    }

    private fun applyRetentionPeriod(period: DataRetentionPeriod) {
        viewModelScope.launch {
            setRetentionPeriod(period)
            purgeOldRecords()
            analyticsLogger.retentionPeriodChanged(period.name.lowercase())
        }
    }

    private fun fileTimestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}
