package com.example.timeselfie.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.timeselfie.data.database.dao.CapsuleEntryDao
import com.example.timeselfie.data.database.dao.TimeCapsuleDao
import com.example.timeselfie.data.database.entities.CapsuleEntry
import com.example.timeselfie.data.database.entities.TimeCapsule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class TimeCapsuleRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mockTimeCapsuleDao = mockk<TimeCapsuleDao>()
    private val mockCapsuleEntryDao = mockk<CapsuleEntryDao>()
    private lateinit var repository: TimeCapsuleRepository

    @Before
    fun setup() {
        repository = TimeCapsuleRepository(mockTimeCapsuleDao, mockCapsuleEntryDao)
    }

    @Test
    fun `getActiveCapsule should return active capsule from dao`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )
        coEvery { mockTimeCapsuleDao.getActiveCapsule() } returns mockCapsule

        // When
        val result = repository.getActiveCapsule()

        // Then
        assertEquals(mockCapsule, result)
        coVerify { mockTimeCapsuleDao.getActiveCapsule() }
    }

    @Test
    fun `createNewCapsule should create and return capsule id`() = runTest {
        // Given
        val capsuleName = "Test Capsule"
        val expectedId = 1L
        
        coEvery { mockTimeCapsuleDao.deactivateAllCapsules() } returns Unit
        coEvery { mockTimeCapsuleDao.insertCapsule(any()) } returns expectedId

        // When
        val result = repository.createNewCapsule(capsuleName)

        // Then
        assertEquals(expectedId, result)
        coVerify { mockTimeCapsuleDao.deactivateAllCapsules() }
        coVerify { mockTimeCapsuleDao.insertCapsule(any()) }
    }

    @Test
    fun `getOrCreateActiveCapsule should return existing active capsule`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )
        coEvery { mockTimeCapsuleDao.getActiveCapsule() } returns mockCapsule

        // When
        val result = repository.getOrCreateActiveCapsule()

        // Then
        assertEquals(mockCapsule, result)
        coVerify { mockTimeCapsuleDao.getActiveCapsule() }
    }

    @Test
    fun `getOrCreateActiveCapsule should create new capsule when none exists`() = runTest {
        // Given
        val newCapsuleId = 1L
        val newCapsule = TimeCapsule(
            id = newCapsuleId,
            name = "June 2025",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )
        
        coEvery { mockTimeCapsuleDao.getActiveCapsule() } returns null
        coEvery { mockTimeCapsuleDao.deactivateAllCapsules() } returns Unit
        coEvery { mockTimeCapsuleDao.insertCapsule(any()) } returns newCapsuleId
        coEvery { mockTimeCapsuleDao.getCapsuleById(newCapsuleId) } returns newCapsule

        // When
        val result = repository.getOrCreateActiveCapsule()

        // Then
        assertEquals(newCapsule, result)
        coVerify { mockTimeCapsuleDao.getActiveCapsule() }
        coVerify { mockTimeCapsuleDao.deactivateAllCapsules() }
        coVerify { mockTimeCapsuleDao.insertCapsule(any()) }
        coVerify { mockTimeCapsuleDao.getCapsuleById(newCapsuleId) }
    }

    @Test
    fun `getEntriesForCapsule should return flow from dao`() = runTest {
        // Given
        val capsuleId = 1L
        val mockEntries = listOf(
            CapsuleEntry(
                id = 1L,
                capsuleId = capsuleId,
                date = "2025-06-15",
                dayNumber = 1,
                mood = "happy",
                imagePath = "/path/to/image.jpg",
                imageFileName = "image.jpg"
            )
        )
        coEvery { mockCapsuleEntryDao.getEntriesForCapsule(capsuleId) } returns flowOf(mockEntries)

        // When
        val result = repository.getEntriesForCapsule(capsuleId)

        // Then
        result.collect { entries ->
            assertEquals(mockEntries, entries)
        }
        coVerify { mockCapsuleEntryDao.getEntriesForCapsule(capsuleId) }
    }

    @Test
    fun `addEntryToCapsule should insert entry with correct day number`() = runTest {
        // Given
        val capsuleId = 1L
        val date = "2025-06-15"
        val mood = "happy"
        val imagePath = "/path/to/image.jpg"
        val imageFileName = "image.jpg"
        val maxDayNumber = 5
        
        coEvery { mockCapsuleEntryDao.getMaxDayNumberForCapsule(capsuleId) } returns maxDayNumber
        coEvery { mockCapsuleEntryDao.insertEntry(any()) } returns Unit

        // When
        repository.addEntryToCapsule(capsuleId, date, mood, imagePath, imageFileName)

        // Then
        coVerify { mockCapsuleEntryDao.getMaxDayNumberForCapsule(capsuleId) }
        coVerify { 
            mockCapsuleEntryDao.insertEntry(
                match { entry ->
                    entry.capsuleId == capsuleId &&
                    entry.date == date &&
                    entry.mood == mood &&
                    entry.imagePath == imagePath &&
                    entry.imageFileName == imageFileName &&
                    entry.dayNumber == maxDayNumber + 1
                }
            )
        }
    }

    @Test
    fun `addEntryToCapsule should handle null max day number`() = runTest {
        // Given
        val capsuleId = 1L
        val date = "2025-06-15"
        val mood = "happy"
        val imagePath = "/path/to/image.jpg"
        val imageFileName = "image.jpg"
        
        coEvery { mockCapsuleEntryDao.getMaxDayNumberForCapsule(capsuleId) } returns null
        coEvery { mockCapsuleEntryDao.insertEntry(any()) } returns Unit

        // When
        repository.addEntryToCapsule(capsuleId, date, mood, imagePath, imageFileName)

        // Then
        coVerify { 
            mockCapsuleEntryDao.insertEntry(
                match { entry ->
                    entry.dayNumber == 1 // Should be 1 when no previous entries
                }
            )
        }
    }

    @Test
    fun `getEntryByDate should return entry from dao`() = runTest {
        // Given
        val capsuleId = 1L
        val date = "2025-06-15"
        val mockEntry = CapsuleEntry(
            id = 1L,
            capsuleId = capsuleId,
            date = date,
            dayNumber = 1,
            mood = "happy",
            imagePath = "/path/to/image.jpg",
            imageFileName = "image.jpg"
        )
        
        coEvery { mockCapsuleEntryDao.getEntryByDate(capsuleId, date) } returns mockEntry

        // When
        val result = repository.getEntryByDate(capsuleId, date)

        // Then
        assertEquals(mockEntry, result)
        coVerify { mockCapsuleEntryDao.getEntryByDate(capsuleId, date) }
    }
}
