package com.sangyoon.vehiclenote.feature.entryexit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.sangyoon.vehiclenote.core.ui.components.VnButton
import com.sangyoon.vehiclenote.core.ui.components.VnButtonSize
import com.sangyoon.vehiclenote.core.ui.components.VnButtonStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PlateConfirmDialog(
    plate: String,
    onPlateChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("번호판 확인") },
        text = {
            OutlinedTextField(
                value = plate,
                onValueChange = onPlateChange,
                label = { Text("차량번호") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            VnButton(
                "확인",
                onClick = onConfirm,
                style = VnButtonStyle.Primary,
                size = VnButtonSize.Small,
                enabled = plate.isNotBlank(),
            )
        },
        dismissButton = {
            VnButton(
                "취소",
                onClick = onDismiss,
                style = VnButtonStyle.Ghost,
                size = VnButtonSize.Small,
            )
        }
    )
}
