package com.sangyoon.vehiclenote.feature.vehicle.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.sangyoon.vehiclenote.core.ui.components.VnButton
import com.sangyoon.vehiclenote.core.ui.components.VnButtonSize
import com.sangyoon.vehiclenote.core.ui.components.VnButtonStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sangyoon.vehiclenote.domain.model.CustomField

@Composable
fun CustomFieldsSection(
    customFields: List<CustomField>,
    onAddField: () -> Unit,
    onRemoveField: (Int) -> Unit,
    onKeyChanged: (Int, String) -> Unit,
    onValueChanged: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                "  추가 필드  ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        customFields.forEachIndexed { index, field ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = field.key,
                    onValueChange = { onKeyChanged(index, it) },
                    label = { Text("항목명") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = field.value,
                    onValueChange = { onValueChanged(index, it) },
                    label = { Text("내용") },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true
                )
                IconButton(onClick = { onRemoveField(index) }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "필드 삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        VnButton(
            text = "+ 필드 추가",
            onClick = onAddField,
            style = VnButtonStyle.Ghost,
            size = VnButtonSize.Small,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
