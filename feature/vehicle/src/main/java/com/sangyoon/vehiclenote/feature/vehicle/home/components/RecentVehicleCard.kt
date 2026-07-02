package com.sangyoon.vehiclenote.feature.vehicle.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.core.ui.components.Plate
import com.sangyoon.vehiclenote.core.ui.components.PlateSize
import com.sangyoon.vehiclenote.core.ui.theme.VnTypeBodySm
import com.sangyoon.vehiclenote.core.ui.theme.VnTypeMonoTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentVehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = modifier.width(140.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Plate(value = vehicle.licensePlate, size = PlateSize.Sm)

            Text(
                text = vehicle.ownerName,
                style = VnTypeBodySm,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Text(
                text = formatRelativeTime(vehicle.createdAt),
                style = VnTypeMonoTime,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000      -> "방금 전"
        diff < 3_600_000   -> "${diff / 60_000}분 전"
        diff < 86_400_000  -> "${diff / 3_600_000}시간 전"
        diff < 604_800_000 -> "${diff / 86_400_000}일 전"
        else -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9F5)
@Composable
private fun RecentVehicleCardPreview() {
    RecentVehicleCard(
        vehicle = Vehicle(id = 1, licensePlate = "12가1234", ownerName = "채상윤"),
        onClick = {},
    )
}
