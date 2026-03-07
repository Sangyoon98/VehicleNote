package com.sangyoon.vehiclenote.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVehicleScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditVehicleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // SideEffect 수집: NavigateBack은 일회성 이벤트
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is EditVehicleSideEffect.NavigateBack -> onNavigateBack()
                is EditVehicleSideEffect.ShowSnackbar -> { /* 필요 시 SnackbarHost 연결 */ }
            }
        }
    }

    EditVehicleScreenContent(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditVehicleScreenContent(
    state: EditVehicleState,
    onAction: (EditVehicleAction) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("차량 수정") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading && state.licensePlate.isBlank()) {
            // 초기 로딩 (데이터 아직 미수신)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.licensePlate,
                    onValueChange = { onAction(EditVehicleAction.LicensePlateChanged(it)) },
                    label = { Text("차량번호 *") },
                    placeholder = { Text("예: 12가1234") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.licensePlateError != null,
                    supportingText = state.licensePlateError?.let { { Text(it) } },
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.ownerName,
                    onValueChange = { onAction(EditVehicleAction.OwnerNameChanged(it)) },
                    label = { Text("차주명 *") },
                    placeholder = { Text("예: 홍길동") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.ownerNameError != null,
                    supportingText = state.ownerNameError?.let { { Text(it) } },
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.department,
                    onValueChange = { onAction(EditVehicleAction.DepartmentChanged(it)) },
                    label = { Text("소속부서 *") },
                    placeholder = { Text("예: 총무부") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.departmentError != null,
                    supportingText = state.departmentError?.let { { Text(it) } },
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = { onAction(EditVehicleAction.PhoneNumberChanged(it)) },
                    label = { Text("연락처") },
                    placeholder = { Text("예: 010-1234-5678") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.carModel,
                    onValueChange = { onAction(EditVehicleAction.CarModelChanged(it)) },
                    label = { Text("차종") },
                    placeholder = { Text("예: 그랜저") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.memo,
                    onValueChange = { onAction(EditVehicleAction.MemoChanged(it)) },
                    label = { Text("메모") },
                    placeholder = { Text("추가 정보를 입력하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onAction(EditVehicleAction.SaveClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading && state.licensePlate.isNotBlank()) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("수정하기", style = MaterialTheme.typography.titleMedium)
                    }
                }

                state.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
