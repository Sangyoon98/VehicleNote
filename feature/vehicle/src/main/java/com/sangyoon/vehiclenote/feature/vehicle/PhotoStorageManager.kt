package com.sangyoon.vehiclenote.feature.vehicle

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 차량 사진 파일 관리 싱글톤.
 *
 * 모든 사진은 앱 내부 저장소(`files/photos/`)에 저장되어 외부 앱에서 직접 접근할 수 없다.
 * 카메라/갤러리에 공유할 때는 FileProvider를 통해 임시 Uri를 발급한다.
 */
@Singleton
class PhotoStorageManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /**
     * 카메라 촬영 결과를 저장할 빈 파일과 FileProvider Uri를 생성한다.
     *
     * 반환된 Uri는 `ActivityResultContracts.TakePicture()`에 전달되고,
     * 파일 경로는 촬영 완료 후 [Vehicle.photoPath]로 저장된다.
     *
     * @return (내부 저장소 파일, FileProvider Uri) 쌍.
     */
    fun createCameraOutputFile(): Pair<File, Uri> {
        val dir = File(context.filesDir, "photos").also { it.mkdirs() }
        val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return file to uri
    }

    /**
     * 갤러리에서 선택한 이미지를 앱 내부 저장소로 복사한다.
     *
     * 원본 Uri는 ContentResolver로만 접근 가능하므로, 내부 파일로 복사해 영구 보관한다.
     * IO 스레드에서 실행되며 실패 시 null을 반환한다.
     *
     * @param sourceUri 갤러리에서 선택한 이미지의 콘텐츠 Uri.
     * @return 복사된 파일의 절대 경로, 실패 시 null.
     */
    suspend fun copyGalleryImageToInternal(sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "photos").also { it.mkdirs() }
            val dest = File(dir, "photo_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        }.getOrNull()
    }

    /**
     * 내부 저장소의 사진 파일을 삭제한다.
     *
     * 파일이 존재하지 않으면 아무 작업도 수행하지 않는다.
     * 차량 삭제 또는 사진 교체 시 기존 파일을 정리하기 위해 호출된다.
     *
     * @param path 삭제할 파일의 절대 경로.
     */
    fun deletePhoto(path: String) {
        File(path).takeIf { it.exists() }?.delete()
    }
}
