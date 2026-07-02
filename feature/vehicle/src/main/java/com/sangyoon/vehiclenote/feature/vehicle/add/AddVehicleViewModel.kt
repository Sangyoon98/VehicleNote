package com.sangyoon.vehiclenote.feature.vehicle.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.usecase.AddVehicleUseCase
import com.sangyoon.vehiclenote.core.analytics.AnalyticsLogger
import com.sangyoon.vehiclenote.feature.vehicle.PhotoStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 차량 등록 화면 ViewModel (MVI 패턴).
 *
 * 폼 입력, 사진 촬영/갤러리 선택, 유효성 검사, DB 저장을 담당한다.
 * 카메라 촬영 시 [PhotoStorageManager.createCameraOutputFile]로 미리 파일을 생성하고,
 * 취소되면 해당 파일을 삭제해 저장소 낭비를 방지한다.
 *
 * 네비게이션 인수 `licensePlate`를 [SavedStateHandle]에서 읽어 초기값으로 설정한다 (입출차 화면 연동).
 *
 * 상태: [AddVehicleState], 사이드이펙트: [AddVehicleSideEffect]
 */
@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val addVehicleUseCase: AddVehicleUseCase,
    private val photoStorageManager: PhotoStorageManager,
    private val analyticsLogger: AnalyticsLogger,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(
        AddVehicleState(
            licensePlate = savedStateHandle.get<String>("licensePlate") ?: ""
        )
    )
    val state: StateFlow<AddVehicleState> = _state.asStateFlow()

    private val _sideEffect = Channel<AddVehicleSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onAction(action: AddVehicleAction) {
        _state.update { it.reduce(action) }

        when (action) {
            AddVehicleAction.SaveClicked -> saveVehicle()
            AddVehicleAction.CameraSelected -> launchCamera()
            AddVehicleAction.GallerySelected -> sendSideEffect(AddVehicleSideEffect.LaunchGallery)
            is AddVehicleAction.CameraResultReceived -> handleCameraResult(action.success)
            is AddVehicleAction.GalleryResultReceived -> handleGalleryResult(action.uri)
            AddVehicleAction.PhotoRemoved -> removePhoto()
            AddVehicleAction.CameraPermissionDenied -> handleCameraPermissionDenied()
            else -> Unit
        }
    }

    private fun launchCamera() {
        val (file, uri) = photoStorageManager.createCameraOutputFile()
        _state.update {
            it.copy(
                pendingCameraFilePath = file.absolutePath,
                previousPhotoPath = it.photoPath,
                showPhotoSourceDialog = false
            )
        }
        sendSideEffect(AddVehicleSideEffect.LaunchCamera(uri))
    }

    private fun handleCameraResult(success: Boolean) {
        if (success) {
            _state.update {
                it.copy(
                    photoPath = it.pendingCameraFilePath,
                    pendingCameraFilePath = null,
                    previousPhotoPath = null
                )
            }
        } else {
            // 촬영 취소: 대기 파일 삭제 후 이전 사진 복원
            _state.value.pendingCameraFilePath?.let { photoStorageManager.deletePhoto(it) }
            _state.update {
                it.copy(
                    photoPath = it.previousPhotoPath,
                    pendingCameraFilePath = null,
                    previousPhotoPath = null
                )
            }
        }
    }

    private fun handleCameraPermissionDenied() {
        // 권한 거부: 대기 파일 삭제 후 이전 사진 복원 + 안내 메시지
        _state.value.pendingCameraFilePath?.let { photoStorageManager.deletePhoto(it) }
        _state.update {
            it.copy(
                photoPath = it.previousPhotoPath,
                pendingCameraFilePath = null,
                previousPhotoPath = null
            )
        }
        sendSideEffect(AddVehicleSideEffect.ShowSnackbar("카메라 권한이 필요합니다. 설정에서 허용해 주세요."))
    }

    private fun handleGalleryResult(uri: android.net.Uri?) {
        if (uri != null) {
            viewModelScope.launch {
                val path = photoStorageManager.copyGalleryImageToInternal(uri)
                _state.value.photoPath?.let { photoStorageManager.deletePhoto(it) }
                _state.update { it.copy(photoPath = path) }
            }
        }
    }

    private fun removePhoto() {
        _state.value.photoPath?.let { photoStorageManager.deletePhoto(it) }
        _state.update { it.copy(photoPath = null) }
    }

    private fun saveVehicle() {
        if (validateInput()) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val current = _state.value
            val vehicle = Vehicle(
                licensePlate = current.licensePlate.trim(),
                ownerName = current.ownerName.trim(),
                department = current.department.trim().ifBlank { null },
                phoneNumber = current.phoneNumber.trim().ifBlank { null },
                carModel = current.carModel.trim().ifBlank { null },
                memo = current.memo.trim().ifBlank { null },
                photoPath = current.photoPath,
                customFields = current.customFields.filter { it.key.isNotBlank() }
            )

            addVehicleUseCase(vehicle).fold(
                onSuccess = {
                    analyticsLogger.vehicleRegistered(vehicle.licensePlate)
                    _state.update { it.copy(isLoading = false) }
                    _sideEffect.send(AddVehicleSideEffect.NavigateBack)
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, error = error.message ?: "저장 실패") }
                }
            )
        }
    }

    private fun validateInput(): Boolean {
        var hasError = false
        val current = _state.value

        if (current.licensePlate.isBlank()) {
            _state.update { it.copy(licensePlateError = "차량번호를 입력해주세요.") }
            hasError = true
        }
        if (current.ownerName.isBlank()) {
            _state.update { it.copy(ownerNameError = "차주명을 입력해주세요.") }
            hasError = true
        }

        return hasError
    }

    private fun sendSideEffect(effect: AddVehicleSideEffect) {
        viewModelScope.launch { _sideEffect.send(effect) }
    }
}
