package com.sangyoon.vehiclenote.domain.usecase

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.RecordType
import com.sangyoon.vehiclenote.domain.repository.FakeEntryExitRepository
import com.sangyoon.vehiclenote.domain.repository.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PurgeOldRecordsUseCaseTest {
    private lateinit var entryExitRepo: FakeEntryExitRepository
    private lateinit var settingsRepo: FakeSettingsRepository
    private lateinit var useCase: PurgeOldRecordsUseCase

    @Before
    fun setup() {
        entryExitRepo = FakeEntryExitRepository()
        settingsRepo = FakeSettingsRepository(DataRetentionPeriod.ONE_DAY)
        useCase = PurgeOldRecordsUseCase(entryExitRepo, settingsRepo)
    }

    @Test
    fun `UNLIMITED이면 기록을 삭제하지 않는다`() = runTest {
        // Given
        settingsRepo.setRetentionPeriod(DataRetentionPeriod.UNLIMITED)
        entryExitRepo.insertRecord(
            EntryExitRecord(licensePlate = "12가1234", type = RecordType.ENTRY, timestamp = 0L)
        )

        // When
        useCase()

        // Then
        assertNull(entryExitRepo.deleteBeforeTimestamp)
        assertEquals(1, entryExitRepo.getAllRecords().first().size)
    }

    @Test
    fun `ONE_DAY이면 1일 이전 기록을 삭제한다`() = runTest {
        // Given
        settingsRepo.setRetentionPeriod(DataRetentionPeriod.ONE_DAY)
        val now = System.currentTimeMillis()
        val oldTimestamp = now - 2 * 24L * 60 * 60 * 1000
        entryExitRepo.insertRecord(
            EntryExitRecord(licensePlate = "12가1234", type = RecordType.ENTRY, timestamp = oldTimestamp)
        )

        // When
        useCase()

        // Then
        assertEquals(0, entryExitRepo.getAllRecords().first().size)
    }

    @Test
    fun `최근 기록은 삭제하지 않는다`() = runTest {
        // Given
        settingsRepo.setRetentionPeriod(DataRetentionPeriod.ONE_WEEK)
        val now = System.currentTimeMillis()
        val recentTimestamp = now - 1 * 24L * 60 * 60 * 1000 // 1일 전 (7일 보관 기간 내)
        entryExitRepo.insertRecord(
            EntryExitRecord(licensePlate = "12가1234", type = RecordType.ENTRY, timestamp = recentTimestamp)
        )

        // When
        useCase()

        // Then
        assertEquals(1, entryExitRepo.getAllRecords().first().size)
    }

    @Test
    fun `ONE_MONTH이면 30일 이전 기록을 삭제한다`() = runTest {
        // Given
        settingsRepo.setRetentionPeriod(DataRetentionPeriod.ONE_MONTH)
        val now = System.currentTimeMillis()
        val oldTimestamp = now - 31 * 24L * 60 * 60 * 1000
        val recentTimestamp = now - 10 * 24L * 60 * 60 * 1000
        entryExitRepo.insertRecord(
            EntryExitRecord(licensePlate = "12가1234", type = RecordType.ENTRY, timestamp = oldTimestamp)
        )
        entryExitRepo.insertRecord(
            EntryExitRecord(licensePlate = "99나9999", type = RecordType.EXIT, timestamp = recentTimestamp)
        )

        // When
        useCase()

        // Then
        val remaining = entryExitRepo.getAllRecords().first()
        assertEquals(1, remaining.size)
        assertEquals("99나9999", remaining[0].licensePlate)
    }
}
