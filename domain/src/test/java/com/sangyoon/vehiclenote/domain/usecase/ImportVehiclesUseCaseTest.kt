package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.FakeVehicleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ImportVehiclesUseCaseTest {

    private lateinit var fakeRepository: FakeVehicleRepository
    private lateinit var importVehiclesUseCase: ImportVehiclesUseCase
    private lateinit var getAllVehiclesUseCase: GetAllVehiclesUseCase

    @Before
    fun setup() {
        fakeRepository = FakeVehicleRepository()
        importVehiclesUseCase = ImportVehiclesUseCase(fakeRepository)
        getAllVehiclesUseCase = GetAllVehiclesUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        fakeRepository.clear()
    }

    private fun vehicle(plate: String, owner: String = "홍길동") = Vehicle(
        id = 0,
        licensePlate = plate,
        ownerName = owner
    )

    @Test
    fun `새 차량 목록을 모두 등록한다`() = runTest {
        val result = importVehiclesUseCase(
            listOf(vehicle("12가1234"), vehicle("34나5678"))
        )

        assertEquals(2, result.addedCount)
        assertEquals(0, result.skippedByDuplicate)
        assertEquals(2, getAllVehiclesUseCase().first().size)
    }

    @Test
    fun `이미 등록된 번호판은 건너뛴다`() = runTest {
        fakeRepository.insertVehicle(vehicle("12가1234", owner = "기존차주"))

        val result = importVehiclesUseCase(
            listOf(vehicle("12가1234", owner = "새차주"), vehicle("34나5678"))
        )

        assertEquals(1, result.addedCount)
        assertEquals(1, result.skippedByDuplicate)
        // 기존 데이터가 덮어써지지 않아야 한다
        val existing = getAllVehiclesUseCase().first().first { it.licensePlate == "12가1234" }
        assertEquals("기존차주", existing.ownerName)
    }

    @Test
    fun `빈 목록이면 아무것도 등록하지 않는다`() = runTest {
        val result = importVehiclesUseCase(emptyList())

        assertEquals(0, result.addedCount)
        assertEquals(0, result.skippedByDuplicate)
        assertEquals(0, getAllVehiclesUseCase().first().size)
    }
}
