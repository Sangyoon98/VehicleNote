package com.sangyoon.vehiclenote.ui.add

import android.net.Uri

/** 차량 등록 화면 사용자 액션. */
sealed interface AddVehicleAction {
    /** 차량번호 입력값 변경. */
    data class LicensePlateChanged(val value: String) : AddVehicleAction
    /** 차주명 입력값 변경. */
    data class OwnerNameChanged(val value: String) : AddVehicleAction
    /** 부서 입력값 변경. */
    data class DepartmentChanged(val value: String) : AddVehicleAction
    /** 전화번호 입력값 변경. */
    data class PhoneNumberChanged(val value: String) : AddVehicleAction
    /** 차종 입력값 변경. */
    data class CarModelChanged(val value: String) : AddVehicleAction
    /** 메모 입력값 변경. */
    data class MemoChanged(val value: String) : AddVehicleAction
    /** 저장 버튼 클릭. 유효성 검사 후 DB 저장 요청. */
    data object SaveClicked : AddVehicleAction

    // 사진 관련
    /** 사진 영역 클릭. 소스 선택 다이얼로그 표시. */
    data object PhotoClicked : AddVehicleAction
    /** 카메라 촬영 선택. */
    data object CameraSelected : AddVehicleAction
    /** 갤러리 선택. */
    data object GallerySelected : AddVehicleAction
    /** 카메라 촬영 완료. success=false이면 파일 삭제 후 이전 사진 복원. */
    data class CameraResultReceived(val success: Boolean) : AddVehicleAction
    /** 갤러리 선택 완료. uri=null이면 취소 처리. */
    data class GalleryResultReceived(val uri: Uri?) : AddVehicleAction
    /** 사진 소스 다이얼로그 닫기. */
    data object PhotoSourceDialogDismissed : AddVehicleAction
    /** 등록된 사진 삭제. */
    data object PhotoRemoved : AddVehicleAction
    /** 카메라 권한 거부됨. */
    data object CameraPermissionDenied : AddVehicleAction

    // 커스텀 필드 관련
    /** 커스텀 필드 행 추가. */
    data object AddCustomField : AddVehicleAction
    /** 커스텀 필드 행 삭제. */
    data class RemoveCustomField(val index: Int) : AddVehicleAction
    /** 커스텀 필드 키(필드명) 변경. */
    data class CustomFieldKeyChanged(val index: Int, val key: String) : AddVehicleAction
    /** 커스텀 필드 값 변경. */
    data class CustomFieldValueChanged(val index: Int, val value: String) : AddVehicleAction
}
