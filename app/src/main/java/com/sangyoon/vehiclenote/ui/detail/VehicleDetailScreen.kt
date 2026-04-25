package com.sangyoon.vehiclenote.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import com.sangyoon.vehiclenote.ui.components.VnButton
import com.sangyoon.vehiclenote.ui.components.VnButtonSize
import com.sangyoon.vehiclenote.ui.components.VnButtonStyle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import com.sangyoon.vehiclenote.ui.components.VnTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sangyoon.vehiclenote.ui.detail.components.VehicleDetailContent

@Composable
fun VehicleDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: VehicleDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is VehicleDetailSideEffect.NavigateBack -> onNavigateBack()
                is VehicleDetailSideEffect.NavigateToEdit -> onNavigateToEdit(effect.vehicleId)
                is VehicleDetailSideEffect.ShowSnackbar -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    VehicleDetailScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreenContent(
    state: VehicleDetailState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateBack: () -> Unit = {},
    onAction: (VehicleDetailAction) -> Unit = {}
) {
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onAction(VehicleDetailAction.DismissDeleteDialog) },
            title = { Text("차량 삭제") },
            text = { Text("${state.vehicle?.licensePlate} 차량을 삭제하시겠습니까?") },
            confirmButton = {
                VnButton(
                    "삭제",
                    onClick = { onAction(VehicleDetailAction.DeleteConfirmed) },
                    style = VnButtonStyle.Danger,
                    size = VnButtonSize.Small,
                )
            },
            dismissButton = {
                VnButton(
                    "취소",
                    onClick = { onAction(VehicleDetailAction.DismissDeleteDialog) },
                    style = VnButtonStyle.Ghost,
                    size = VnButtonSize.Small,
                )
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { VnTopBar(title = "차량 상세", onNavigateBack = onNavigateBack) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = state.error, color = MaterialTheme.colorScheme.error)
                        VnButton("돌아가기", onClick = onNavigateBack)
                    }
                }

                state.vehicle != null -> {
                    VehicleDetailContent(
                        vehicle = state.vehicle,
                        onEditClick = { onAction(VehicleDetailAction.EditClicked) },
                        onDeleteClick = { onAction(VehicleDetailAction.ShowDeleteDialog) }
                    )
                }
            }
        }
    }
}

