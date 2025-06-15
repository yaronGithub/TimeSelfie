package com.example.timeselfie.ui.screens.daily

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.timeselfie.data.database.entities.CapsuleEntry
import com.example.timeselfie.data.database.entities.TimeCapsule
import com.example.timeselfie.data.repository.TimeCapsuleRepository
import com.example.timeselfie.utils.storage.ImageStorage
import com.example.timeselfie.utils.storage.SaveResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class DailyEntryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository = mockk<TimeCapsuleRepository>()
    private val mockImageStorage = mockk<ImageStorage>()
    private lateinit var viewModel: DailyEntryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onMoodChanged should update mood in uiState`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )

        coEvery { mockRepository.getOrCreateActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntryByDate(any(), any()) } returns null

        viewModel = DailyEntryViewModel(mockRepository, mockImageStorage)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onMoodChanged("happy")

        // Then
        assertEquals("happy", viewModel.uiState.value.mood)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `onImageCaptured should update capturedImageUri in uiState`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )
        val mockUri = mockk<Uri>()

        coEvery { mockRepository.getOrCreateActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntryByDate(any(), any()) } returns null

        viewModel = DailyEntryViewModel(mockRepository, mockImageStorage)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onImageCaptured(mockUri)

        // Then
        assertEquals(mockUri, viewModel.uiState.value.capturedImageUri)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `onSaveEntry should save entry successfully`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )
        val mockUri = mockk<Uri>()
        val saveResult = SaveResult.Success(
            imagePath = "/path/to/image.jpg",
            thumbnailPath = "/path/to/thumb.jpg",
            fileName = "image.jpg"
        )

        coEvery { mockRepository.getOrCreateActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntryByDate(any(), any()) } returns null
        coEvery { mockImageStorage.saveSelfie(any(), any()) } returns saveResult
        coEvery { mockRepository.addEntryToCapsule(any(), any(), any(), any(), any()) } returns Unit

        viewModel = DailyEntryViewModel(mockRepository, mockImageStorage)
        testDispatcher.scheduler.advanceUntilIdle()

        // Set up the state for saving
        viewModel.onImageCaptured(mockUri)
        viewModel.onMoodChanged("happy")

        // When
        viewModel.onSaveEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.isSaved)
        assertTrue(uiState.hasExistingEntry)
        assertNull(uiState.error)

        // Verify repository calls
        coVerify { mockImageStorage.saveSelfie(mockUri, any()) }
        coVerify { 
            mockRepository.addEntryToCapsule(
                capsuleId = 1L,
                date = any(),
                mood = "happy",
                imagePath = "/path/to/image.jpg",
                imageFileName = "image.jpg"
            )
        }
    }

    @Test
    fun `onSaveEntry should handle save error`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )
        val mockUri = mockk<Uri>()
        val saveResult = SaveResult.Error("Failed to save image")

        coEvery { mockRepository.getOrCreateActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntryByDate(any(), any()) } returns null
        coEvery { mockImageStorage.saveSelfie(any(), any()) } returns saveResult

        viewModel = DailyEntryViewModel(mockRepository, mockImageStorage)
        testDispatcher.scheduler.advanceUntilIdle()

        // Set up the state for saving
        viewModel.onImageCaptured(mockUri)
        viewModel.onMoodChanged("happy")

        // When
        viewModel.onSaveEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isSaved)
        assertEquals("Failed to save image", uiState.error)
    }

    @Test
    fun `canSave should return true when image and mood are provided`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )
        val mockUri = mockk<Uri>()

        coEvery { mockRepository.getOrCreateActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntryByDate(any(), any()) } returns null

        viewModel = DailyEntryViewModel(mockRepository, mockImageStorage)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onImageCaptured(mockUri)
        viewModel.onMoodChanged("happy")

        // Then
        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun `canSave should return false when image or mood is missing`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )

        coEvery { mockRepository.getOrCreateActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntryByDate(any(), any()) } returns null

        viewModel = DailyEntryViewModel(mockRepository, mockImageStorage)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - only mood, no image
        viewModel.onMoodChanged("happy")

        // Then
        assertFalse(viewModel.uiState.value.canSave)
    }
}
