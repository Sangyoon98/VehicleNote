package com.sangyoon.vehiclenote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.sangyoon.vehiclenote.ui.components.VnButton
import com.sangyoon.vehiclenote.ui.components.VnButtonSize
import com.sangyoon.vehiclenote.ui.components.VnButtonStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PhotoSourceDialog(
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "사진 추가",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "사진을 어디서 가져올까요?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // 카메라
                VnButton(
                    text = "카메라",
                    onClick = onCameraSelected,
                    leadingIcon = Icons.Default.CameraAlt,
                    size = VnButtonSize.Large,
                    fullWidth = true,
                )

                // 갤러리
                VnButton(
                    text = "갤러리",
                    onClick = onGallerySelected,
                    leadingIcon = Icons.Default.Photo,
                    size = VnButtonSize.Large,
                    fullWidth = true,
                )

                // 취소
                VnButton(
                    text = "취소",
                    onClick = onDismiss,
                    style = VnButtonStyle.Outline,
                    size = VnButtonSize.Large,
                    fullWidth = true,
                )
            }
        }
    }
}
