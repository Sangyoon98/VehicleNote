package com.sangyoon.vehiclenote.ui.detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TextFields
import com.sangyoon.vehiclenote.ui.components.VnButton
import com.sangyoon.vehiclenote.ui.components.VnButtonSize
import com.sangyoon.vehiclenote.ui.components.VnButtonStyle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.ui.components.ImageViewerDialog
import com.sangyoon.vehiclenote.ui.components.InfoRow
import com.sangyoon.vehiclenote.ui.components.Plate
import com.sangyoon.vehiclenote.ui.components.PlateSize
import com.sangyoon.vehiclenote.ui.theme.VnTypeCaption
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VehicleDetailContent(
    vehicle: Vehicle,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showImageViewer by remember { mutableStateOf(false) }

    if (showImageViewer && vehicle.photoPath != null) {
        ImageViewerDialog(
            photoPath = vehicle.photoPath!!,
            onDismiss = { showImageViewer = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 사진
        vehicle.photoPath?.let { path ->
            AsyncImage(
                model = File(path),
                contentDescription = "차량 사진",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showImageViewer = true }
            )
        }

        // 번호판 히어로
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "차량 번호",
                style = VnTypeCaption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Plate(value = vehicle.licensePlate, size = PlateSize.Xl, animated = true)
        }

        // 기본 정보 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = com.sangyoon.vehiclenote.ui.theme.VnSurface
            ),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, com.sangyoon.vehiclenote.ui.theme.VnLine)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoRow(label = "차주명", value = vehicle.ownerName)
                vehicle.phoneNumber?.let {
                    InfoRow(label = "연락처", value = it, mono = true)
                }
                vehicle.department?.let {
                    InfoRow(label = "소속", value = it)
                }
                vehicle.carModel?.let {
                    InfoRow(label = "차종", value = it)
                }
                vehicle.memo?.let {
                    InfoRow(label = "메모", value = it)
                }
            }
        }

        // 커스텀 필드 카드
        if (vehicle.customFields.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "추가 정보",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider()
                    vehicle.customFields.forEach { field ->
                        InfoRow(label = field.key, value = field.value)
                    }
                }
            }
        }

        // 등록 정보 (간소화)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "등록일시: ${formatDate(vehicle.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 수정 / 삭제 버튼 (세로로 배치)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VnButton(
                text = "편집하기",
                onClick = onEditClick,
                style = VnButtonStyle.Primary,
                size = VnButtonSize.Large,
                leadingIcon = Icons.Default.Edit,
                fullWidth = true,
            )
            VnButton(
                text = "삭제하기",
                onClick = onDeleteClick,
                style = VnButtonStyle.Danger,
                size = VnButtonSize.Large,
                leadingIcon = Icons.Default.Delete,
                fullWidth = true,
            )
        }
    }
}


internal fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
