package com.sangyoon.vehiclenote.ui.entryexit

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sangyoon.ocr.CameraPreviewWithRecognition
import com.sangyoon.vehiclenote.di.OcrEntryPoint
import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.RecordType
import com.sangyoon.vehiclenote.ui.components.Plate
import com.sangyoon.vehiclenote.ui.components.PlateSize
import com.sangyoon.vehiclenote.ui.components.VnButton
import com.sangyoon.vehiclenote.ui.components.VnButtonSize
import com.sangyoon.vehiclenote.ui.components.VnButtonStyle
import com.sangyoon.vehiclenote.ui.components.VnStatusTag
import com.sangyoon.vehiclenote.ui.components.toTagKind
import com.sangyoon.vehiclenote.ui.entryexit.components.CameraReticle
import com.sangyoon.vehiclenote.ui.entryexit.components.CameraStatusBar
import com.sangyoon.vehiclenote.ui.entryexit.components.ManualInputDialog
import com.sangyoon.vehiclenote.ui.entryexit.components.PlateConfirmDialog
import com.sangyoon.vehiclenote.ui.theme.VnTypeMonoTime
import dagger.hilt.android.EntryPointAccessors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryExitScreen(
    parentContentPadding: PaddingValues = PaddingValues(),
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToLogList: () -> Unit,
    viewModel: EntryExitViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

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
        if (!hasCameraPermission) cameraPermissionLauncher.launch(cameraPermission)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is EntryExitSideEffect.NavigateToDetail -> onNavigateToDetail(effect.recordId)
                is EntryExitSideEffect.NavigateToLogList -> onNavigateToLogList()
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (hasCameraPermission) {
                CameraPreviewWithRecognition(
                    plateRecognizer = plateRecognizer,
                    onPlateDetected = { plate ->
                        viewModel.onAction(EntryExitAction.PlateDetected(plate))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {},
                    // 다이얼로그 처리 중 다음 차량 인식 결과가 버려지지 않도록 분석 일시 중지
                    analysisEnabled = !state.showPlateConfirmDialog && !state.showManualInputDialog,
                )

                // 상단 상태바 오버레이
                CameraStatusBar(modifier = Modifier.align(Alignment.TopCenter))

                // 번호판 인식 리티클 (화면 중앙보다 위쪽에 배치)
                CameraReticle(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 80.dp),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "입출차 번호판 인식을 위해\n카메라 권한이 필요합니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                    )
                    VnButton(
                        text = "카메라 권한 요청",
                        onClick = { cameraPermissionLauncher.launch(cameraPermission) },
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }

            // 하단 오버레이
            val systemNavBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val appNavBar = (parentContentPadding.calculateBottomPadding() - systemNavBar).coerceAtLeast(0.dp)
            val bottomPadding = systemNavBar + appNavBar + 16.dp

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                    )
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = bottomPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 최근 기록 (최대 2개)
                AnimatedVisibility(
                    visible = state.records.isNotEmpty(),
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300)),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.records.take(2).forEach { record ->
                            RecentRecordRow(
                                record = record,
                                onClick = { viewModel.onAction(EntryExitAction.RecordClicked(record.id)) }
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                // 액션 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    VnButton(
                        text = "직접 입력",
                        onClick = { viewModel.onAction(EntryExitAction.ManualInputClicked) },
                        style = VnButtonStyle.Secondary,
                        size = VnButtonSize.Small,
                        leadingIcon = Icons.Filled.Edit,
                    )
                    VnButton(
                        text = "전체 기록 보기",
                        onClick = { viewModel.onAction(EntryExitAction.LogListClicked) },
                        style = VnButtonStyle.Ghost,
                        size = VnButtonSize.Small,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentRecordRow(
    record: EntryExitRecord,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Plate(value = record.licensePlate, size = PlateSize.Sm)

        Text(
            text = formatTimestamp(record.timestamp),
            style = VnTypeMonoTime,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )

        VnStatusTag(
            kind = record.type.toTagKind(),
            text = if (record.type == RecordType.ENTRY) "입차" else "출차",
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
