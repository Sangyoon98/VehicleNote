package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.FakeVehicleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UpdateVehicleUseCaseTest {

    private lateinit var fakeRepository: FakeVehicleRepository
    private lateinit var updateVehicleUseCase: UpdateVehicleUseCase
    private lateinit var getAllVehiclesUseCase: GetAllVehiclesUseCase

    @Before
    fun setup() {
        fakeRepository = FakeVehicleRepository()
        updateVehicleUseCase = UpdateVehicleUseCase(fakeRepository)
        getAllVehiclesUseCase = GetAllVehiclesUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        fakeRepository.clear()
    }

    @Test
    fun `차량 정보를 성공적으로 업데이트한다`() = runTest {
        // Given
        val vehicle = Vehicle(
            id = 0,
            licensePlate = "12가1234",
            ownerName = "홍길동",
            department = "총무부"
        )
        val insertedId = fakeRepository.insertVehicle(vehicle)

        val updatedVehicle = vehicle.copy(
            id = insertedId,
            ownerName = "김철수",
            department = "인사부",
            phoneNumber = "010-9999-9999"
        )

        // When
        val result = updateVehicleUseCase(updatedVehicle)

        // Then
        assertTrue(result.isSuccess)
        val vehicles = getAllVehiclesUseCase().first()
        assertEquals(1, vehicles.size)
        assertEquals("김철수", vehicles[0].ownerName)
        assertEquals("인사부", vehicles[0].department)
        assertEquals("010-9999-9999", vehicles[0].phoneNumber)
    }

    @Test
    fun `차량번호는 유지되고 다른 정보만 업데이트된다`() = runTest {
        // Given
        val vehicle = Vehicle(
            id = 0,
            licensePlate = "12가1234",
            ownerName = "홍길동",
            department = "총무부"
        )
        val insertedId = fakeRepository.insertVehicle(vehicle)

        val updatedVehicle = vehicle.copy(
            id = insertedId,
            licensePlate = "12가1234",  // 차량번호 동일
            ownerName = "이영희"
        )

        // When
        updateVehicleUseCase(updatedVehicle)

        // Then
        val vehicles = getAllVehiclesUseCase().first()
        assertEquals("12가1234", vehicles[0].licensePlate)
        assertEquals("이영희", vehicles[0].ownerName)
    }

    @Test
    fun `업데이트 시간이 변경된다`() = runTest {
        // Given
        val originalTime = System.currentTimeMillis() - 10000
        val vehicle = Vehicle(
            id = 0,
            licensePlate = "12가1234",
            ownerName = "홍길동",
            department = "총무부",
            createdAt = originalTime,
            updatedAt = originalTime
        )
        val insertedId = fakeRepository.insertVehicle(vehicle)

        Thread.sleep(100)  // 시간 차이 만들기

        val updatedVehicle = vehicle.copy(
            id = insertedId,
            ownerName = "김철수",
            updatedAt = System.currentTimeMillis()
        )

        // When
        updateVehicleUseCase(updatedVehicle)

        // Then
        val vehicles = getAllVehiclesUseCase().first()
        assertTrue(vehicles[0].updatedAt > originalTime)
    }
}