package com.sangyoon.vehiclenote.ui.add

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddVehicleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // 저장 성공 시 뒤로가기
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("차량 등록") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 차량번호 (필수)
            OutlinedTextField(
                value = state.licensePlate,
                onValueChange = { viewModel.onIntent(AddVehicleIntent.LicensePlateChanged(it)) },
                label = { Text("차량번호 *") },
                placeholder = { Text("예: 12가1234") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.licensePlateError != null,
                supportingText = state.licensePlateError?.let { { Text(it) } },
                singleLine = true
            )

            // 차주명 (필수)
            OutlinedTextField(
                value = state.ownerName,
                onValueChange = { viewModel.onIntent(AddVehicleIntent.OwnerNameChanged(it)) },
                label = { Text("차주명 *") },
                placeholder = { Text("예: 홍길동") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.ownerNameError != null,
                supportingText = state.ownerNameError?.let { { Text(it) } },
                singleLine = true
            )

            // 소속부서 (필수)
            OutlinedTextField(
                value = state.department,
                onValueChange = { viewModel.onIntent(AddVehicleIntent.DepartmentChanged(it)) },
                label = { Text("소속부서 *") },
                placeholder = { Text("예: 총무부") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.departmentError != null,
                supportingText = state.departmentError?.let { { Text(it) } },
                singleLine = true
            )

            // 연락처 (선택)
            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = { viewModel.onIntent(AddVehicleIntent.PhoneNumberChanged(it)) },
                label = { Text("연락처") },
                placeholder = { Text("예: 010-1234-5678") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            // 차종 (선택)
            OutlinedTextField(
                value = state.carModel,
                onValueChange = { viewModel.onIntent(AddVehicleIntent.CarModelChanged(it)) },
                label = { Text("차종") },
                placeholder = { Text("예: 그랜저") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 메모 (선택)
            OutlinedTextField(
                value = state.memo,
                onValueChange = { viewModel.onIntent(AddVehicleIntent.MemoChanged(it)) },
                label = { Text("메모") },
                placeholder = { Text("추가 정보를 입력하세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 저장 버튼
            Button(
                onClick = { viewModel.onIntent(AddVehicleIntent.SaveClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("등록하기", style = MaterialTheme.typography.titleMedium)
                }
            }

            // 에러 메시지
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

@Preview
@Composable
fun AddVehicleScreenPreview() {
    AddVehicleScreen(
        onNavigateBack = {}
    )
}