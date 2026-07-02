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

class DeleteVehicleUseCaseTest {
    private lateinit var fakeRepository: FakeVehicleRepository
    private lateinit var deleteVehicleUseCase: DeleteVehicleUseCase
    private lateinit var getAllVehiclesUseCase: GetAllVehiclesUseCase

    @Before
    fun setup() {
        fakeRepository = FakeVehicleRepository()
        deleteVehicleUseCase = DeleteVehicleUseCase(fakeRepository)
        getAllVehiclesUseCase = GetAllVehiclesUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        fakeRepository.clear()
    }

    @Test
    fun `차량을 성공적으로 삭제한다`() = runTest {
        // Given
        val vehicle = Vehicle(
            id = 0,
            licensePlate = "12가1234",
            ownerName = "홍길동",
            department = "총무부"
        )
        val insertedId = fakeRepository.insertVehicle(vehicle)
        val insertVehicle = vehicle.copy(id = insertedId)

        // When
        val result = deleteVehicleUseCase(insertVehicle)

        // Then
        assertTrue(result.isSuccess)
        val vehicles = getAllVehiclesUseCase().first()
        assertEquals(0, vehicles.size)
    }

    @Test
    fun `여러 차량 중 특정 차량만 삭제한다`() = runTest {
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

        val id1 = fakeRepository.insertVehicle(vehicle1)
        val id2 = fakeRepository.insertVehicle(vehicle2)

        // When
        deleteVehicleUseCase(vehicle1.copy(id = id1))

        // Then
        val vehicles = getAllVehiclesUseCase().first()
        assertEquals(1, vehicles.size)
        assertEquals("34나5678", vehicles[0].licensePlate)
    }

    @Test
    fun `존재하지 않는 차량 삭제 시 성공을 반환한다`() = runTest {
        // Given
        val nonExistentVehicle = Vehicle(
            id = 999,
            licensePlate = "99하9999",
            ownerName = "없는사람",
            department = "없는부서"
        )

        // When
        val result = deleteVehicleUseCase(nonExistentVehicle)

        // Then
        assertTrue(result.isSuccess)
    }
}