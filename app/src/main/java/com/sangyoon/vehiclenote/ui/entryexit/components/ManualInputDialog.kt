package com.sangyoon.vehiclenote.ui.entryexit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ManualInputDialog(
    plate: String,
    onPlateChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("차량번호 직접 입력") },
        text = {
            OutlinedTextField(
                value = plate,
                onValueChange = onPlateChange,
                label = { Text("차량번호") },
                placeholder = { Text("예: 12가1234") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = plate.isNotBlank()
            ) {
                Text("입차/출차 처리")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
