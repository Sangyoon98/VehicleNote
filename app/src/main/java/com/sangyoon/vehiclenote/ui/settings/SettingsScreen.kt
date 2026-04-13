package com.sangyoon.vehiclenote.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("설정") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = parentContentPadding.calculateBottomPadding())
        ) {
            SettingsItem(
                title = "입출차 기록 저장 기간",
                subtitle = state.retentionPeriod.label(),
                onClick = { viewModel.onAction(SettingsAction.OnRetentionPeriodClicked) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }

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
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                        Text(
                            text = period.label(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
private fun UnlimitedWarningDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("주의") },
        text = {
            Text(
                "무한 저장을 선택하면 기록이 계속 쌓여 저장 공간을 많이 차지할 수 있습니다.\n계속 진행하시겠습니까?"
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("확인") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

private fun DataRetentionPeriod.label(): String = when (this) {
    DataRetentionPeriod.ONE_DAY -> "1일"
    DataRetentionPeriod.ONE_WEEK -> "1주일"
    DataRetentionPeriod.ONE_MONTH -> "1달"
    DataRetentionPeriod.UNLIMITED -> "무한"
}
