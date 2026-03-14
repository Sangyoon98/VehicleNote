package com.sangyoon.vehiclenote.ui.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.model.CustomField
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.usecase.AddVehicleUseCase
import com.sangyoon.vehiclenote.util.PhotoStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val addVehicleUseCase: AddVehicleUseCase,
    private val photoStorageManager: PhotoStorageManager,
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
        when (action) {
            is AddVehicleAction.LicensePlateChanged ->
                _state.update { it.copy(licensePlate = action.value, licensePlateError = null) }

            is AddVehicleAction.OwnerNameChanged ->
                _state.update { it.copy(ownerName = action.value, ownerNameError = null) }

            is AddVehicleAction.DepartmentChanged ->
                _state.update { it.copy(department = action.value) }

            is AddVehicleAction.PhoneNumberChanged ->
                _state.update { it.copy(phoneNumber = action.value) }

            is AddVehicleAction.CarModelChanged ->
                _state.update { it.copy(carModel = action.value) }

            is AddVehicleAction.MemoChanged ->
                _state.update { it.copy(memo = action.value) }

            AddVehicleAction.SaveClicked -> saveVehicle()

            AddVehicleAction.PhotoClicked ->
                _state.update { it.copy(showPhotoSourceDialog = true) }

            AddVehicleAction.PhotoSourceDialogDismissed ->
                _state.update { it.copy(showPhotoSourceDialog = false) }

            AddVehicleAction.CameraSelected -> launchCamera()
            AddVehicleAction.GallerySelected -> {
                _state.update { it.copy(showPhotoSourceDialog = false) }
                sendSideEffect(AddVehicleSideEffect.LaunchGallery)
            }

            is AddVehicleAction.CameraResultReceived -> handleCameraResult(action.success)
            is AddVehicleAction.GalleryResultReceived -> handleGalleryResult(action.uri)
            AddVehicleAction.PhotoRemoved -> removePhoto()
            AddVehicleAction.CameraPermissionDenied -> handleCameraPermissionDenied()

            AddVehicleAction.AddCustomField ->
                _state.update { it.copy(customFields = it.customFields + CustomField("", "")) }

            is AddVehicleAction.RemoveCustomField ->
                _state.update {
                    it.copy(
                        customFields = it.customFields.toMutableList()
                            .also { list -> list.removeAt(action.index) })
                }

            is AddVehicleAction.CustomFieldKeyChanged ->
                _state.update {
                    it.copy(customFields = it.customFields.toMutableList().also { list ->
                        list[action.index] = list[action.index].copy(key = action.key)
                    })
                }

            is AddVehicleAction.CustomFieldValueChanged ->
                _state.update {
                    it.copy(customFields = it.customFields.toMutableList().also { list ->
                        list[action.index] = list[action.index].copy(value = action.value)
                    })
                }
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
