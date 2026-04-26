package com.sangyoon.vehiclenote.ui.add

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.sangyoon.vehiclenote.ui.components.VnButton
import com.sangyoon.vehiclenote.ui.components.VnButtonSize
import com.sangyoon.vehiclenote.ui.components.VnButtonStyle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import com.sangyoon.vehiclenote.ui.components.VnTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sangyoon.vehiclenote.ui.components.CustomFieldsSection
import com.sangyoon.vehiclenote.ui.components.PhotoSection
import com.sangyoon.vehiclenote.ui.components.PhotoSourceDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddVehicleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // 카메라 실행 후 결과 수신
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        viewModel.onAction(AddVehicleAction.CameraResultReceived(success))
    }

    // 카메라 권한 요청 → 허용 시 바로 카메라 실행, 거부 시 Action 전달
    var pendingCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingCameraUri?.let { cameraLauncher.launch(it) }
            pendingCameraUri = null
        } else {
            pendingCameraUri = null
            viewModel.onAction(AddVehicleAction.CameraPermissionDenied)
        }
    }

    // 갤러리 (API 33+: PickVisualMedia, 구버전: GetContent)
    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.onAction(AddVehicleAction.GalleryResultReceived(uri))
    }
    val getContentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onAction(AddVehicleAction.GalleryResultReceived(uri))
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                AddVehicleSideEffect.NavigateBack -> onNavigateBack()
                is AddVehicleSideEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is AddVehicleSideEffect.LaunchCamera -> {
                    val permission = Manifest.permission.CAMERA
                    if (ContextCompat.checkSelfPermission(
                            context,
                            permission
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // 권한 있음: 바로 카메라 실행
                        cameraLauncher.launch(effect.outputUri)
                    } else {
                        // 권한 없음: 요청 후 결과에 따라 처리
                        pendingCameraUri = effect.outputUri
                        cameraPermissionLauncher.launch(permission)
                    }
                }

                AddVehicleSideEffect.LaunchGallery -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickMediaLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    } else {
                        getContentLauncher.launch("image/*")
                    }
                }
            }
        }
    }

    if (state.showPhotoSourceDialog) {
        PhotoSourceDialog(
            onCameraSelected = { viewModel.onAction(AddVehicleAction.CameraSelected) },
            onGallerySelected = { viewModel.onAction(AddVehicleAction.GallerySelected) },
            onDismiss = { viewModel.onAction(AddVehicleAction.PhotoSourceDialogDismissed) }
        )
    }

    Scaffold(
        topBar = { VnTopBar(title = "차량 등록", onNavigateBack = onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PhotoSection(
                photoPath = state.photoPath,
                onPhotoClicked = { viewModel.onAction(AddVehicleAction.PhotoClicked) },
                onPhotoRemoved = { viewModel.onAction(AddVehicleAction.PhotoRemoved) }
            )

            OutlinedTextField(
                value = state.licensePlate,
                onValueChange = { viewModel.onAction(AddVehicleAction.LicensePlateChanged(it)) },
                label = { Text("차량번호 *") },
                placeholder = { Text("예: 12가1234") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.licensePlateError != null,
                supportingText = state.licensePlateError?.let { { Text(it) } },
                singleLine = true
            )

            OutlinedTextField(
                value = state.ownerName,
                onValueChange = { viewModel.onAction(AddVehicleAction.OwnerNameChanged(it)) },
                label = { Text("차주명 *") },
                placeholder = { Text("예: 홍길동") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.ownerNameError != null,
                supportingText = state.ownerNameError?.let { { Text(it) } },
                singleLine = true
            )

            OutlinedTextField(
                value = state.department,
                onValueChange = { viewModel.onAction(AddVehicleAction.DepartmentChanged(it)) },
                label = { Text("소속부서") },
                placeholder = { Text("예: 총무부") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = { viewModel.onAction(AddVehicleAction.PhoneNumberChanged(it)) },
                label = { Text("연락처") },
                placeholder = { Text("예: 010-1234-5678") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            OutlinedTextField(
                value = state.carModel,
                onValueChange = { viewModel.onAction(AddVehicleAction.CarModelChanged(it)) },
                label = { Text("차종") },
                placeholder = { Text("예: 그랜저") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.memo,
                onValueChange = { viewModel.onAction(AddVehicleAction.MemoChanged(it)) },
                label = { Text("메모") },
                placeholder = { Text("추가 정보를 입력하세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            CustomFieldsSection(
                customFields = state.customFields,
                onAddField = { viewModel.onAction(AddVehicleAction.AddCustomField) },
                onRemoveField = { viewModel.onAction(AddVehicleAction.RemoveCustomField(it)) },
                onKeyChanged = { index, key ->
                    viewModel.onAction(
                        AddVehicleAction.CustomFieldKeyChanged(
                            index,
                            key
                        )
                    )
                },
                onValueChanged = { index, value ->
                    viewModel.onAction(
                        AddVehicleAction.CustomFieldValueChanged(
                            index,
                            value
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            VnButton(
                text = "등록하기",
                onClick = { viewModel.onAction(AddVehicleAction.SaveClicked) },
                style = VnButtonStyle.Primary,
                size = VnButtonSize.Large,
                fullWidth = true,
                enabled = !state.isLoading,
                isLoading = state.isLoading,
            )

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

