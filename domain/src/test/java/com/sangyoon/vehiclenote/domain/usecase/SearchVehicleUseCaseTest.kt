package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.Vehicle
import com.sangyoon.vehiclenote.domain.repository.FakeVehicleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchVehicleUseCaseTest {
    private lateinit var fakeRepository: FakeVehicleRepository
    private lateinit var searchVehicleUseCase: SearchVehicleUseCase

    @Before
    fun setup() {
        fakeRepository = FakeVehicleRepository()
        searchVehicleUseCase = SearchVehicleUseCase(fakeRepository)

        // 테스트 데이터 준비
        runTest {
            fakeRepository.insertVehicle(
                Vehicle(0, "12가1234", "홍길동", "총무부")
            )
            fakeRepository.insertVehicle(
                Vehicle(0, "34나5678", "김철수", "인사부")
            )
            fakeRepository.insertVehicle(
                Vehicle(0, "12다9012", "이영희", "개발부")
            )
        }
    }

    @After
    fun tearDown() {
        fakeRepository.clear()
    }

    @Test
    fun `차량번호로 검색한다`() = runTest {
        // When
        val result = searchVehicleUseCase("12가").first()

        // Then
        assertEquals(1, result.size)
        assertEquals("12가1234", result[0].licensePlate)
    }

    @Test
    fun `부분 검색이 가능하다`() = runTest {
        // When
        val result = searchVehicleUseCase("12").first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.any { it.licensePlate == "12가1234" })
        assertTrue(result.any { it.licensePlate == "12다9012" })
    }

    @Test
    fun `검색 결과가 없으면 빈 리스트를 반환한다`() = runTest {
        // When
        val result = searchVehicleUseCase("99하9999").first()

        // Then
        assertEquals(0, result.size)
    }
}