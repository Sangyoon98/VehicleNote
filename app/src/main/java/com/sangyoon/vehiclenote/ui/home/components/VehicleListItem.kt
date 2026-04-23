package com.sangyoon.vehiclenote.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.sangyoon.vehiclenote.ui.components.Plate
import com.sangyoon.vehiclenote.ui.components.PlateSize
import com.sangyoon.vehiclenote.ui.theme.VnInkMute
import com.sangyoon.vehiclenote.ui.theme.VnTypeBodySm

@Composable
fun VehicleListItem(
    modifier: Modifier = Modifier,
    vehicle: Vehicle,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 좌측: 번호판 배지
            Plate(value = vehicle.licensePlate, size = PlateSize.Sm)

            // 우측: 차주명 + 부서/전화
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = vehicle.ownerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val sub = listOfNotNull(vehicle.department, vehicle.phoneNumber).joinToString(" · ")
                if (sub.isNotEmpty()) {
                    Text(
                        text = sub,
                        style = VnTypeBodySm,
                        color = VnInkMute,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9F5)
@Composable
private fun VehicleListItemPreview() {
    VehicleListItem(
        vehicle = Vehicle(
            id = 1,
            licensePlate = "12가1234",
            ownerName = "채상윤",
            department = "개발팀",
            phoneNumber = "010-4736-3559",
            carModel = "벨로스터",
            memo = "테스트",
        ),
        onClick = {},
        onDelete = {},
    )
}
