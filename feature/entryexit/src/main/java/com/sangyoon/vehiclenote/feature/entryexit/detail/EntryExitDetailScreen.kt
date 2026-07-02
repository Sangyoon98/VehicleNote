package com.sangyoon.vehiclenote.feature.entryexit.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.sangyoon.vehiclenote.core.ui.components.InfoRow
import com.sangyoon.vehiclenote.core.ui.components.Plate
import com.sangyoon.vehiclenote.core.ui.components.PlateSize
import com.sangyoon.vehiclenote.core.ui.components.VnButton
import com.sangyoon.vehiclenote.core.ui.components.VnStatusTag
import com.sangyoon.vehiclenote.core.ui.components.toTagKind
import com.sangyoon.vehiclenote.core.ui.theme.VnEntryBg
import com.sangyoon.vehiclenote.core.ui.theme.VnEntryInk
import com.sangyoon.vehiclenote.core.ui.theme.VnExitBg
import com.sangyoon.vehiclenote.core.ui.theme.VnExitInk
import com.sangyoon.vehiclenote.core.ui.theme.VnTypeCaption
import com.sangyoon.vehiclenote.core.ui.theme.VnTypeDisplay
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.sangyoon.vehiclenote.core.ui.components.VnTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sangyoon.vehiclenote.domain.model.RecordType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryExitDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddVehicle: (String) -> Unit,
    viewModel: EntryExitDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                EntryExitDetailSideEffect.NavigateBack -> onNavigateBack()
                is EntryExitDetailSideEffect.NavigateToAddVehicle ->
                    onNavigateToAddVehicle(effect.licensePlate)
            }
        }
    }

    Scaffold(
        topBar = {
            VnTopBar(
                title = "입출차 상세",
                onNavigateBack = { viewModel.onAction(EntryExitDetailAction.NavigateBackClicked) },
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        VnButton("돌아가기", onClick = onNavigateBack)
                    }
                }

                state.record != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        val record = state.record!!
                        val isEntry = record.type == RecordType.ENTRY

                        // 티켓 카드 — 입차(amber) / 출차(teal) + 찢어낸 노치
                        Surface(
                            shape = TicketShape(notchRadiusDp = 10f),
                            color = if (isEntry) VnEntryBg else VnExitBg,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = androidx.compose.ui.Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = if (isEntry) "입차 등록" else "출차 등록",
                                    style = VnTypeCaption,
                                    color = if (isEntry) VnEntryInk else VnExitInk,
                                )
                                Text(
                                    text = formatTime(record.timestamp),
                                    style = VnTypeDisplay,
                                    color = if (isEntry) VnEntryInk else VnExitInk,
                                )
                            }
                        }

                        // 번호판 히어로
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "차량 번호",
                                style = VnTypeCaption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Plate(
                                value = record.licensePlate,
                                size = PlateSize.Xl,
                                variant = if (isEntry)
                                    com.sangyoon.vehiclenote.core.ui.components.PlateVariant.Entry
                                else
                                    com.sangyoon.vehiclenote.core.ui.components.PlateVariant.Exit,
                                animated = true,
                            )
                        }

                        // 미등록 차량 경고 또는 차량 정보
                        if (state.vehicle == null) {
                            // 미등록 차량 경고
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "미등록 차량입니다",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            "차량 정보를 등록하여 관리하세요",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 차량 등록 버튼
                            VnButton(
                                text = "차량 등록하기",
                                onClick = { viewModel.onAction(EntryExitDetailAction.RegisterVehicleClicked) },
                                fullWidth = true,
                            )
                        }

                        // 입차 정보 카드
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "입차 정보",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                HorizontalDivider()

                                InfoRow(
                                    label = "입차 시각",
                                    value = formatDate(record.timestamp),
                                    mono = true,
                                )
                                InfoRow(
                                    label = "입차 방법",
                                    value = "자동 인식 (OCR)",
                                )
                            }
                        }

                        // 차량 정보 카드 (등록된 차량인 경우)
                        state.vehicle?.let { vehicle ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        "차량 정보",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    HorizontalDivider()

                                    InfoRow(label = "차주명", value = vehicle.ownerName)
                                    vehicle.department?.let { InfoRow(label = "소속", value = it) }
                                    vehicle.phoneNumber?.let { InfoRow(label = "연락처", value = it, mono = true) }
                                    vehicle.carModel?.let { InfoRow(label = "차종", value = it) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * 찢어낸 티켓 모양. 좌우 중간에 반원 노치를 뺀 둥근 사각형.
 */
private class TicketShape(private val notchRadiusDp: Float) : Shape {
    override fun createOutline(size: androidx.compose.ui.geometry.Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val notchR = with(density) { notchRadiusDp.dp.toPx() }
        val cornerR = with(density) { 14.dp.toPx() }
        val midY = size.height / 2f

        val mainPath = Path().apply {
            addRoundRect(RoundRect(Rect(Offset.Zero, size), CornerRadius(cornerR)))
        }
        val leftNotch = Path().apply {
            addOval(Rect(left = -notchR, top = midY - notchR, right = notchR, bottom = midY + notchR))
        }
        val rightNotch = Path().apply {
            addOval(Rect(left = size.width - notchR, top = midY - notchR, right = size.width + notchR, bottom = midY + notchR))
        }

        val step1 = Path().apply { op(mainPath, leftNotch, PathOperation.Difference) }
        val result = Path().apply { op(step1, rightNotch, PathOperation.Difference) }
        return Outline.Generic(result)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy년 MM월 dd일 (E) HH:mm", Locale.KOREAN)
    return sdf.format(Date(timestamp))
}
