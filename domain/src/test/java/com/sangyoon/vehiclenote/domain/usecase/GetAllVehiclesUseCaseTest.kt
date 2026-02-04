package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.FakeVehicleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetAllVehiclesUseCaseTest {
    private lateinit var fakeRepository: FakeVehicleRepository
    private lateinit var getAllVehiclesUseCase: GetAllVehiclesUseCase

    @Before
    fun setup() {
        fakeRepository = FakeVehicleRepository()
        getAllVehiclesUseCase = GetAllVehiclesUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        fakeRepository.clear()
    }

    @Test
    fun `빈 리리스트를 반환한다`() = runTest {
        // When
        val result = getAllVehiclesUseCase().first()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `차량 목록을 반환한다`() = runTest {
        // Given
        val vehicle1 = Vehicle(
            id = 0,
            licensePlate = "12가1234",
            ownerName = "홍길동",
            department = "총무부"
        )

        val vehicle2 = Vehicle(
            id = 0,
            licensePlate = "34나5678",
            ownerName = "김철수",
            department = "인사부"
        )

        fakeRepository.insertVehicle(vehicle1)
        fakeRepository.insertVehicle(vehicle2)

        // When
        val result = getAllVehiclesUseCase().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("12가1234", result[0].licensePlate)
        assertEquals("34나5678", result[1].licensePlate)
    }
}