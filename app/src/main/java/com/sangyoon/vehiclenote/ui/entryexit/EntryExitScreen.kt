package com.sangyoon.vehiclenote.ui.entryexit

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sangyoon.ocr.CameraPreviewWithRecognition
import com.sangyoon.vehiclenote.di.OcrEntryPoint
import com.sangyoon.vehiclenote.ui.components.VnButton
import com.sangyoon.vehiclenote.ui.components.VnButtonSize
import com.sangyoon.vehiclenote.ui.components.VnButtonStyle
import com.sangyoon.vehiclenote.ui.entryexit.components.EntryExitLogList
import com.sangyoon.vehiclenote.ui.entryexit.components.ManualInputDialog
import com.sangyoon.vehiclenote.ui.entryexit.components.PlateConfirmDialog
import dagger.hilt.android.EntryPointAccessors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryExitScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: EntryExitViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val plateRecognizer = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            OcrEntryPoint::class.java
        ).plateRecognizer()
    }

    DisposableEffect(plateRecognizer) {
        onDispose { plateRecognizer.close() }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(cameraPermission)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is EntryExitSideEffect.NavigateToDetail -> onNavigateToDetail(effect.recordId)
                is EntryExitSideEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    if (state.showPlateConfirmDialog) {
        PlateConfirmDialog(
            plate = state.detectedPlate,
            onPlateChange = { viewModel.onAction(EntryExitAction.DetectedPlateEdited(it)) },
            onConfirm = { viewModel.onAction(EntryExitAction.PlateConfirmed) },
            onDismiss = { viewModel.onAction(EntryExitAction.PlateConfirmDismissed) }
        )
    }

    if (state.showManualInputDialog) {
        ManualInputDialog(
            plate = state.manualInputPlate,
            onPlateChange = { viewModel.onAction(EntryExitAction.ManualPlateChanged(it)) },
            onConfirm = { viewModel.onAction(EntryExitAction.ManualInputConfirmed) },
            onDismiss = { viewModel.onAction(EntryExitAction.ManualInputDismissed) }
        )
    }

    val sheetScaffoldState = rememberBottomSheetScaffoldState()
    // 시트가 완전히 펼쳐진 경우에만 리스트 스크롤 허용.
    // 그 외에는 스크롤 이벤트가 LazyColumn에서 소비되지 않고 M3 NestedScrollConnection으로
    // 전달되어 시트가 손가락을 자연스럽게 따라 움직인다.
    val listScrollEnabled by remember {
        derivedStateOf { sheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded }
    }

    BottomSheetScaffold(
        scaffoldState = sheetScaffoldState,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetPeekHeight = 280.dp,
        sheetContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "입출차 기록",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (state.records.isNotEmpty()) {
                        Text(
                            text = "${state.records.size}건",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                VnButton(
                    text = "직접 입력",
                    onClick = { viewModel.onAction(EntryExitAction.ManualInputClicked) },
                    style = VnButtonStyle.Secondary,
                    size = VnButtonSize.Small,
                    leadingIcon = Icons.Filled.Edit,
                )
            }
            HorizontalDivider()
            EntryExitLogList(
                records = state.records,
                onRecordClick = { recordId ->
                    viewModel.onAction(EntryExitAction.RecordClicked(recordId))
                },
                modifier = Modifier.weight(1f),
                userScrollEnabled = listScrollEnabled,
            )
        },
    ) { _ ->
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (hasCameraPermission) {
                CameraPreviewWithRecognition(
                    plateRecognizer = plateRecognizer,
                    onPlateDetected = { plate ->
                        viewModel.onAction(EntryExitAction.PlateDetected(plate))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {},
                    overlay = {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(0.7f)
                                .aspectRatio(3f)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                        Text(
                            text = "번호판을 사각형 안에 맞추세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 300.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "입출차 번호판 인식을 위해\n카메라 권한이 필요합니다.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    VnButton(
                        text = "카메라 권한 요청",
                        onClick = { cameraPermissionLauncher.launch(cameraPermission) },
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
        }
    }
}
