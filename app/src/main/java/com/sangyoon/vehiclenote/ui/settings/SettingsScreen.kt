package com.sangyoon.vehiclenote.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    parentContentPadding: PaddingValues = PaddingValues(),
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onAction(SettingsAction.OnImportFilePicked(it)) }
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is SettingsSideEffect.ShareVehicleCsv -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, effect.uri)
                        putExtra(Intent.EXTRA_SUBJECT, effect.fileName)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "차량 데이터 내보내기"))
                }
                SettingsSideEffect.LaunchVehicleFilePicker -> {
                    filePicker.launch(arrayOf("text/*", "*/*"))
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("설정") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(bottom = parentContentPadding.calculateBottomPadding())
        ) {
            // ── 기록 관리 섹션 ─────────────────────────────────────────────────
            SectionHeader("기록 관리")
            SettingsItem(
                title = "입출차 기록 저장 기간",
                subtitle = state.retentionPeriod.label(),
                onClick = { viewModel.onAction(SettingsAction.OnRetentionPeriodClicked) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // ── 데이터 관리 섹션 ──────────────────────────────────────────────
            SectionHeader("데이터 관리")
            SettingsItem(
                title = "차량 데이터 내보내기",
                subtitle = "등록된 차량을 CSV 파일로 내보냅니다",
                onClick = { viewModel.onAction(SettingsAction.OnExportVehiclesClicked) },
                trailingContent = if (state.isExportingVehicles) {
                    { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                } else null
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(
                title = "차량 데이터 가져오기",
                subtitle = "CSV 파일에서 차량을 가져옵니다 (사진 제외)",
                onClick = { viewModel.onAction(SettingsAction.OnImportVehiclesClicked) },
                trailingContent = if (state.isImportingVehicles) {
                    { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                } else null
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }

    // ── 다이얼로그 ─────────────────────────────────────────────────────────────
    if (state.showRetentionDialog) {
        RetentionPeriodDialog(
            currentPeriod = state.retentionPeriod,
            onPeriodSelected = { viewModel.onAction(SettingsAction.OnPeriodSelected(it)) },
            onDismiss = { viewModel.onAction(SettingsAction.OnRetentionDialogDismissed) }
        )
    }

    if (state.showUnlimitedWarningDialog) {
        UnlimitedWarningDialog(
            onConfirm = { viewModel.onAction(SettingsAction.OnUnlimitedWarningConfirmed) },
            onDismiss = { viewModel.onAction(SettingsAction.OnUnlimitedWarningDismissed) }
        )
    }

    if (state.showImportConfirmDialog) {
        val total = (state.pendingImportVehicles?.size ?: 0) + state.pendingSkippedRows
        ImportConfirmDialog(
            parsedCount = state.pendingImportVehicles?.size ?: 0,
            totalRows = total,
            skippedRows = state.pendingSkippedRows,
            onConfirm = { viewModel.onAction(SettingsAction.OnImportConfirmed) },
            onDismiss = { viewModel.onAction(SettingsAction.OnImportDialogDismissed) }
        )
    }

    state.importResult?.let { result ->
        ImportResultDialog(
            result = result,
            onDismiss = { viewModel.onAction(SettingsAction.OnImportResultDismissed) }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailingContent?.let {
            Spacer(modifier = Modifier.width(8.dp))
            Box { it() }
        }
    }
}

@Composable
private fun RetentionPeriodDialog(
    currentPeriod: DataRetentionPeriod,
    onPeriodSelected: (DataRetentionPeriod) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("기록 저장 기간 선택") },
        text = {
            Column {
                DataRetentionPeriod.entries.forEach { period ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPeriodSelected(period) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = period == currentPeriod,
                            onClick = { onPeriodSelected(period) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = period.label(), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
private fun UnlimitedWarningDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("주의") },
        text = { Text("무한 저장을 선택하면 기록이 계속 쌓여 저장 공간을 많이 차지할 수 있습니다.\n계속 진행하시겠습니까?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("확인") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
private fun ImportConfirmDialog(
    parsedCount: Int,
    totalRows: Int,
    skippedRows: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("차량 데이터 가져오기") },
        text = {
            Column {
                Text("${parsedCount}개 차량을 가져옵니다.")
                if (skippedRows > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "필수 항목(번호판·차주명) 누락으로 ${skippedRows}개 행이 제외됩니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "이미 등록된 번호판은 건너뜁니다. 사진은 가져오지 않습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("가져오기") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
private fun ImportResultDialog(result: VehicleImportResult, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("가져오기 완료") },
        text = {
            Column {
                Text("${result.addedCount}개 차량을 추가했습니다.")
                if (result.skippedByDuplicate > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "이미 등록된 번호판 ${result.skippedByDuplicate}개는 건너뛰었습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("확인") } }
    )
}

private fun DataRetentionPeriod.label(): String = when (this) {
    DataRetentionPeriod.ONE_DAY -> "1일"
    DataRetentionPeriod.ONE_WEEK -> "1주일"
    DataRetentionPeriod.ONE_MONTH -> "1달"
    DataRetentionPeriod.UNLIMITED -> "무한"
}
