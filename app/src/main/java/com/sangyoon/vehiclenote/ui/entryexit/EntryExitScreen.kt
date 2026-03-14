package com.sangyoon.vehiclenote.ui.entryexit

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangyoon.ocr.CameraPreviewWithRecognition
import com.sangyoon.vehiclenote.di.OcrEntryPoint
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

    // OcrEntryPoint를 통해 PlateRecognizer 주입
    val plateRecognizer = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            OcrEntryPoint::class.java
        ).plateRecognizer()
    }

    // PlateRecognizer 리소스 해제
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

    // 다이얼로그
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
        topBar = {
            TopAppBar(
                title = {
                    if (state.isSearching) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = {
                                viewModel.onAction(
                                    EntryExitAction.SearchQueryChanged(
                                        it
                                    )
                                )
                            },
                            placeholder = { Text("번호판 또는 차주명 검색") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("입출차")
                    }
                },
                actions = {
                    if (state.isSearching) {
                        TextButton(onClick = { viewModel.onAction(EntryExitAction.SearchDismissed) }) {
                            Text("취소")
                        }
                    } else {
                        IconButton(onClick = { viewModel.onAction(EntryExitAction.SearchToggled) }) {
                            Icon(Icons.Filled.Search, contentDescription = "검색")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 카메라 프리뷰 (검색 중이 아닐 때만 표시)
            if (!state.isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clipToBounds()
                ) {
                    if (hasCameraPermission) {
                        CameraPreviewWithRecognition(
                            plateRecognizer = plateRecognizer,
                            onPlateDetected = { plate ->
                                viewModel.onAction(EntryExitAction.PlateDetected(plate))
                            },
                            modifier = Modifier.fillMaxSize(),
                            overlay = {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    IconButton(
                                        onClick = { viewModel.onAction(EntryExitAction.ManualInputClicked) }
                                    ) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = "수동 입력",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "입출차 번호판 인식을 위해 카메라 권한이 필요합니다.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = { cameraPermissionLauncher.launch(cameraPermission) },
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Text("카메라 권한 요청")
                            }
                            TextButton(
                                onClick = { viewModel.onAction(EntryExitAction.ManualInputClicked) }
                            ) {
                                Text("수동 입력으로 진행")
                            }
                        }
                    }
                }
            }

            // 입출차 기록 목록
            EntryExitLogList(
                records = state.records,
                onRecordClick = { recordId ->
                    viewModel.onAction(EntryExitAction.RecordClicked(recordId))
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
