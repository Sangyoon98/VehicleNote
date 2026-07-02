package com.sangyoon.vehiclenote.data.repository

import app.cash.turbine.test
import com.sangyoon.vehiclenote.data.local.dao.VehicleDao
import com.sangyoon.vehiclenote.data.local.entity.VehicleEntity
import com.sangyoon.vehiclenote.domain.model.Vehicle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class VehicleRepositoryImplTest {
    private lateinit var vehicleDao: VehicleDao
    private lateinit var repository: VehicleRepositoryImpl

    @Before
    fun setup() {
        vehicleDao = mockk()
        repository = VehicleRepositoryImpl(vehicleDao)
    }

    @Test
    fun `getAllVehicles는 Entity를 Domain으로 변환하여 반환한다`() = runTest {
        // Given
        val entities = listOf(
            VehicleEntity(
                id = 1,
                licensePlate = "12가1234",
                ownerName = "홍길동",
                department = "총무부",
                phoneNumber = null,
                carModel = null,
                memo = null,
                photoUri = null,
                customFields = "[]",
                createdAt = 123456789,
                updatedAt = 123456789
            ),
            VehicleEntity(
                id = 2,
                licensePlate = "34나5678",
                ownerName = "김철수",
                department = "인사부",
                phoneNumber = "010-1234-5678",
                carModel = "소나타",
                memo = "VIP",
                photoUri = null,
                customFields = "[]",
                createdAt = 123456789,
                updatedAt = 123456789
            )
        )

        coEvery { vehicleDao.getAllVehicles() } returns flowOf(entities)

        // When & Then
        repository.getAllVehicles().test {
            val vehicles = awaitItem()

            assertEquals(2, vehicles.size)
            assertEquals("12가1234", vehicles[0].licensePlate)
            assertEquals("홍길동", vehicles[0].ownerName)
            assertEquals("34나5678", vehicles[1].licensePlate)
            assertEquals("김철수", vehicles[1].ownerName)

            awaitComplete()
        }
    }

    @Test
    fun `search는 검색 결과를 Domain으로 변환한다`() = runTest {
        // Given
        val query = "12가"
        val entities = listOf(
            VehicleEntity(
                id = 1,
                licensePlate = "12가1234",
                ownerName = "홍길동",
                department = "총무부",
                phoneNumber = null,
                carModel = null,
                memo = null,
                photoUri = null,
                customFields = "[]",
                createdAt = 123456789,
                updatedAt = 123456789
            )
        )

        coEvery { vehicleDao.search(query) } returns flowOf(entities)

        // When & Then
        repository.search(query).test {
            val vehicles = awaitItem()

            assertEquals(1, vehicles.size)
            assertEquals("12가1234", vehicles[0].licensePlate)

            awaitComplete()
        }
    }

    @Test
    fun `getVehicleById는 Entity를 Domain으로 변환한다`() = runTest {
        // Given
        val entity = VehicleEntity(
            id = 1,
            licensePlate = "12가1234",
            ownerName = "홍길동",
            department = "총무부",
            phoneNumber = null,
            carModel = null,
            memo = null,
            photoUri = null,
            customFields = "[]",
            createdAt = 123456789,
            updatedAt = 123456789
        )

        coEvery { vehicleDao.getVehicleById(1L) } returns entity

        // When
        val vehicle = repository.getVehicleById(1L)

        // Then
        assertNotNull(vehicle)
        assertEquals("12가1234", vehicle?.licensePlate)
        assertEquals("홍길동", vehicle?.ownerName)
    }

    @Test
    fun `getVehicleById는 없으면 null을 반환한다`() = runTest {
        // Given
        coEvery { vehicleDao.getVehicleById(999L) } returns null

        // When
        val vehicle = repository.getVehicleById(999L)

        // Then
        assertNull(vehicle)
    }

    @Test
    fun `insertVehicle는 Domain을 Entity로 변환하여 저장한다`() = runTest {
        // Given
        val vehicle = Vehicle(
            id = 0,
            licensePlate = "12가1234",
            ownerName = "홍길동",
            department = "총무부",
            phoneNumber = null,
            carModel = null,
            memo = null,
            createdAt = 123456789,
            updatedAt = 123456789
        )

        coEvery { vehicleDao.insertVehicle(any()) } returns 1L

        // When
        val id = repository.insertVehicle(vehicle)

        // Then
        assertEquals(1L, id)
        coVerify { vehicleDao.insertVehicle(any()) }
    }

    @Test
    fun `updateVehicle는 Domain을 Entity로 변환하여 업데이트한다`() = runTest {
        // Given
        val vehicle = Vehicle(
            id = 1,
            licensePlate = "12가1234",
            ownerName = "홍길동",
            department = "총무부",
            phoneNumber = "010-9999-9999",
            carModel = "그랜저",
            memo = "수정됨",
            createdAt = 123456789,
            updatedAt = 987654321
        )

        coEvery { vehicleDao.updateVehicle(any()) } returns Unit

        // When
        repository.updateVehicle(vehicle)

        // Then
        coVerify { vehicleDao.updateVehicle(any()) }
    }

    @Test
    fun `deleteVehicle는 Domain을 Entity로 변환하여 삭제한다`() = runTest {
        // Given
        val vehicle = Vehicle(
            id = 1,
            licensePlate = "12가1234",
            ownerName = "홍길동",
            department = "총무부"
        )

        coEvery { vehicleDao.deleteVehicle(any()) } returns Unit

        // When
        repository.deleteVehicle(vehicle)

        // Then
        coVerify { vehicleDao.deleteVehicle(any()) }
    }
}