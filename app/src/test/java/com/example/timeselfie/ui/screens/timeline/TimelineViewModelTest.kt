package com.example.timeselfie.ui.screens.timeline

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.timeselfie.data.database.entities.CapsuleEntry
import com.example.timeselfie.data.database.entities.TimeCapsule
import com.example.timeselfie.data.models.TimelineItem
import com.example.timeselfie.data.repository.TimeCapsuleRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository = mockk<TimeCapsuleRepository>()
    private lateinit var viewModel: TimelineViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadTimeline should update uiState with timeline items`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )
        
        val mockEntries = listOf(
            CapsuleEntry(
                id = 1L,
                capsuleId = 1L,
                date = "2025-06-15",
                dayNumber = 1,
                mood = "happy",
                imagePath = "/path/to/image.jpg",
                imageFileName = "image.jpg"
            )
        )

        coEvery { mockRepository.getOrCreateActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntriesForCapsule(1L) } returns flowOf(mockEntries)

        // When
        viewModel = TimelineViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        assertEquals("Test Capsule", uiState.currentCapsule)
        assertEquals(30, uiState.entries.size) // Should have 30 days
        
        // Check that the entry with data is properly mapped
        val entryWithData = uiState.entries.find { it.date == "2025-06-15" }
        assertNotNull(entryWithData)
        assertEquals("happy", entryWithData?.mood)
        assertEquals("/path/to/image.jpg", entryWithData?.imagePath)
        assertFalse(entryWithData?.isEmpty ?: true)
    }

    @Test
    fun `loadTimeline should handle repository error`() = runTest {
        // Given
        coEvery { mockRepository.getOrCreateActiveCapsule() } throws Exception("Database error")

        // When
        viewModel = TimelineViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("Database error", uiState.error)
        assertTrue(uiState.entries.isEmpty())
    }

    @Test
    fun `refreshTimeline should reload data`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )

        coEvery { mockRepository.getOrCreateActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntriesForCapsule(1L) } returns flowOf(emptyList())

        viewModel = TimelineViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.refreshTimeline()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        assertEquals("Test Capsule", uiState.currentCapsule)
    }
}
