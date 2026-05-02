package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.RecordType
import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.FakeEntryExitRepository
import com.sangyoon.vehiclenote.domain.repository.FakeVehicleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RecordEntryExitUseCaseTest {
    private lateinit var entryExitRepo: FakeEntryExitRepository
    private lateinit var vehicleRepo: FakeVehicleRepository
    private lateinit var useCase: RecordEntryExitUseCase

    @Before
    fun setup() {
        entryExitRepo = FakeEntryExitRepository()
        vehicleRepo = FakeVehicleRepository()
        useCase = RecordEntryExitUseCase(entryExitRepo, vehicleRepo)
    }

    @Test
    fun `기록이 없으면 입차로 기록한다`() = runTest {
        // Given
        val plate = "12가1234"

        // When
        val result = useCase(plate)

        // Then
        assertEquals(RecordType.ENTRY, result.type)
    }

    @Test
    fun `마지막 기록이 입차이면 출차로 기록한다`() = runTest {
        // Given
        val plate = "12가1234"
        entryExitRepo.insertRecord(
            EntryExitRecord(licensePlate = plate, type = RecordType.ENTRY, timestamp = 1000L)
        )

        // When
        val result = useCase(plate)

        // Then
        assertEquals(RecordType.EXIT, result.type)
    }

    @Test
    fun `마지막 기록이 출차이면 입차로 기록한다`() = runTest {
        // Given
        val plate = "12가1234"
        entryExitRepo.insertRecord(
            EntryExitRecord(licensePlate = plate, type = RecordType.EXIT, timestamp = 1000L)
        )

        // When
        val result = useCase(plate)

        // Then
        assertEquals(RecordType.ENTRY, result.type)
    }

    @Test
    fun `등록된 차량이면 vehicleId를 연결한다`() = runTest {
        // Given
        val plate = "12가1234"
        val vehicle = Vehicle(id = 0, licensePlate = plate, ownerName = "홍길동")
        val addedId = vehicleRepo.insertVehicle(vehicle)

        // When
        val result = useCase(plate)

        // Then
        assertEquals(addedId, result.vehicleId)
        assertEquals("홍길동", result.ownerName)
    }

    @Test
    fun `미등록 차량이면 vehicleId가 null이다`() = runTest {
        // Given
        val plate = "99나9999"

        // When
        val result = useCase(plate)

        // Then
        assertNull(result.vehicleId)
        assertNull(result.ownerName)
    }
}
