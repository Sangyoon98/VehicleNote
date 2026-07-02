package com.sangyoon.vehiclenote.feature.vehicle.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.usecase.GetVehicleByIdUseCase
import com.sangyoon.vehiclenote.domain.usecase.UpdateVehicleUseCase
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
 * 차량 수정 화면 ViewModel (MVI 패턴).
 *
 * [AddVehicleViewModel]과 거의 동일한 구조를 가지나, 초기 진입 시 기존 차량 정보를 로드한다.
 * 네비게이션 인수 `vehicleId`로 차량을 로드하며, 사진 교체/삭제 로직도 동일하게 처리한다.
 *
 * 상태: [EditVehicleState], 사이드이펙트: [EditVehicleSideEffect]
 */
@HiltViewModel
class EditVehicleViewModel @Inject constructor(
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val updateVehicleUseCase: UpdateVehicleUseCase,
    private val photoStorageManager: PhotoStorageManager,
    private val analyticsLogger: AnalyticsLogger,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: Long = checkNotNull(savedStateHandle["vehicleId"])

    private val _state = MutableStateFlow(EditVehicleState())
    val state: StateFlow<EditVehicleState> = _state.asStateFlow()

    private val _sideEffect = Channel<EditVehicleSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadVehicle()
    }

    fun onAction(action: EditVehicleAction) {
        _state.update { it.reduce(action) }

        when (action) {
            EditVehicleAction.SaveClicked -> updateVehicle()
            EditVehicleAction.CameraSelected -> launchCamera()
            EditVehicleAction.GallerySelected -> sendSideEffect(EditVehicleSideEffect.LaunchGallery)
            is EditVehicleAction.CameraResultReceived -> handleCameraResult(action.success)
            is EditVehicleAction.GalleryResultReceived -> handleGalleryResult(action.uri)
            EditVehicleAction.PhotoRemoved -> removePhoto()
            EditVehicleAction.CameraPermissionDenied -> handleCameraPermissionDenied()
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
        sendSideEffect(EditVehicleSideEffect.LaunchCamera(uri))
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
        _state.value.pendingCameraFilePath?.let { photoStorageManager.deletePhoto(it) }
        _state.update {
            it.copy(
                photoPath = it.previousPhotoPath,
                pendingCameraFilePath = null,
                previousPhotoPath = null
            )
        }
        sendSideEffect(EditVehicleSideEffect.ShowSnackbar("카메라 권한이 필요합니다. 설정에서 허용해 주세요."))
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

    private fun loadVehicle() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val vehicle = getVehicleByIdUseCase(vehicleId)
                if (vehicle != null) {
                    _state.update {
                        it.copy(
                            vehicleId = vehicle.id,
                            licensePlate = vehicle.licensePlate,
                            ownerName = vehicle.ownerName,
                            department = vehicle.department ?: "",
                            phoneNumber = vehicle.phoneNumber ?: "",
                            carModel = vehicle.carModel ?: "",
                            memo = vehicle.memo ?: "",
                            photoPath = vehicle.photoPath,
                            customFields = vehicle.customFields,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "차량을 찾을 수 없습니다") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "오류 발생") }
            }
        }
    }

    private fun updateVehicle() {
        if (validateInput()) return

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val current = _state.value
            val vehicle = Vehicle(
                id = current.vehicleId,
                licensePlate = current.licensePlate.trim(),
                ownerName = current.ownerName.trim(),
                department = current.department.trim().ifBlank { null },
                phoneNumber = current.phoneNumber.trim().ifBlank { null },
                carModel = current.carModel.trim().ifBlank { null },
                memo = current.memo.trim().ifBlank { null },
                photoPath = current.photoPath,
                customFields = current.customFields.filter { it.key.isNotBlank() }
            )

            updateVehicleUseCase(vehicle).fold(
                onSuccess = {
                    analyticsLogger.vehicleUpdated(vehicle.licensePlate)
                    _state.update { it.copy(isLoading = false) }
                    _sideEffect.send(EditVehicleSideEffect.NavigateBack)
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, error = error.message ?: "수정 실패") }
                }
            )
        }
    }

    private fun validateInput(): Boolean {
        var hasError = false
        val current = _state.value

        if (current.licensePlate.isBlank()) {
            _state.update { it.copy(licensePlateError = "차량번호를 입력해주세요") }
            hasError = true
        }
        if (current.ownerName.isBlank()) {
            _state.update { it.copy(ownerNameError = "차주명을 입력해주세요") }
            hasError = true
        }

        return hasError
    }

    private fun sendSideEffect(effect: EditVehicleSideEffect) {
        viewModelScope.launch { _sideEffect.send(effect) }
    }
}
