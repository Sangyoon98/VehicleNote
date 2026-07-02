package com.sangyoon.vehiclenote.feature.vehicle.add

import com.sangyoon.vehiclenote.domain.model.CustomField

/**
 * 차량 등록 화면 UI 상태.
 *
 * @property licensePlate 차량번호 입력값 (필수).
 * @property ownerName 차주명 입력값 (필수).
 * @property department 부서 입력값 (선택).
 * @property phoneNumber 전화번호 입력값 (선택).
 * @property carModel 차종 입력값 (선택).
 * @property memo 메모 입력값 (선택).
 * @property photoPath 최종 확정된 사진 내부 저장소 경로. null이면 사진 없음.
 * @property pendingCameraFilePath 카메라 촬영 결과를 기다리는 임시 파일 경로.
 * @property previousPhotoPath 사진 교체 전 이전 경로. 촬영 취소 시 복원에 사용.
 * @property customFields 사용자 정의 추가 필드 목록.
 * @property showPhotoSourceDialog 사진 소스 선택 다이얼로그 표시 여부.
 * @property licensePlateError 차량번호 유효성 검사 에러 메시지. null이면 정상.
 * @property ownerNameError 차주명 유효성 검사 에러 메시지. null이면 정상.
 * @property isLoading 저장 요청 처리 중 여부.
 * @property error 일반 에러 메시지. null이면 정상.
 */
data class AddVehicleState(
    val licensePlate: String = "",
    val ownerName: String = "",
    val department: String = "",
    val phoneNumber: String = "",
    val carModel: String = "",
    val memo: String = "",
    val photoPath: String? = null,
    val pendingCameraFilePath: String? = null,
    val previousPhotoPath: String? = null,
    val customFields: List<CustomField> = emptyList(),
    val showPhotoSourceDialog: Boolean = false,
    val licensePlateError: String? = null,
    val ownerNameError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

fun AddVehicleState.reduce(action: AddVehicleAction): AddVehicleState = when (action) {
    is AddVehicleAction.LicensePlateChanged ->
        copy(licensePlate = action.value, licensePlateError = null)

    is AddVehicleAction.OwnerNameChanged ->
        copy(ownerName = action.value, ownerNameError = null)

    is AddVehicleAction.DepartmentChanged -> copy(department = action.value)
    is AddVehicleAction.PhoneNumberChanged -> copy(phoneNumber = action.value)
    is AddVehicleAction.CarModelChanged -> copy(carModel = action.value)
    is AddVehicleAction.MemoChanged -> copy(memo = action.value)

    AddVehicleAction.PhotoClicked -> copy(showPhotoSourceDialog = true)
    AddVehicleAction.PhotoSourceDialogDismissed -> copy(showPhotoSourceDialog = false)
    AddVehicleAction.GallerySelected -> copy(showPhotoSourceDialog = false)

    AddVehicleAction.AddCustomField ->
        copy(customFields = customFields + CustomField("", ""))

    is AddVehicleAction.RemoveCustomField ->
        copy(customFields = customFields.toMutableList().also { it.removeAt(action.index) })

    is AddVehicleAction.CustomFieldKeyChanged ->
        copy(customFields = customFields.toMutableList().also {
            it[action.index] = it[action.index].copy(key = action.key)
        })

    is AddVehicleAction.CustomFieldValueChanged ->
        copy(customFields = customFields.toMutableList().also {
            it[action.index] = it[action.index].copy(value = action.value)
        })

    else -> this
}
