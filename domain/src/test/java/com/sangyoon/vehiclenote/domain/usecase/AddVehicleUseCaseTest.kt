package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.FakeVehicleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddVehicleUseCaseTest {
    private lateinit var fakeRepository: FakeVehicleRepository
    private lateinit var addVehicleUseCase: AddVehicleUseCase
    private lateinit var getAllVehiclesUseCase: GetAllVehiclesUseCase

    @Before
    fun setup() {
        fakeRepository = FakeVehicleRepository()
        addVehicleUseCase = AddVehicleUseCase(fakeRepository)
        getAllVehiclesUseCase = GetAllVehiclesUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        fakeRepository.clear()
    }

    @Test
    fun `차량을 성공적으로 추가한다`() = runTest {
        // Given
        val vehicle = Vehicle(
            id = 0,
            licensePlate = "12가1234",
            ownerName = "홍길동",
            department = "총무부"
        )

        // When
        val result = addVehicleUseCase(vehicle)

        // Then
        assertTrue(result.isSuccess)
        val vehicles = getAllVehiclesUseCase().first()
        assertEquals(1, vehicles.size)
        assertEquals("12가1234", vehicles[0].licensePlate)
    }

    @Test
    fun `추가된 차량의 ID를 반환한다`() = runTest {
        // Given
        val vehicle = Vehicle(
            id = 0,
            licensePlate = "12가1234",
            ownerName = "홍길동",
            department = "총무부"
        )

        // When
        val result = addVehicleUseCase(vehicle)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
    }
}