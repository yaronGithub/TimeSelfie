package com.example.timeselfie.ui.screens.export

import com.example.timeselfie.data.database.entities.CapsuleEntry
import com.example.timeselfie.data.database.entities.TimeCapsule
import com.example.timeselfie.data.repository.TimeCapsuleRepository
import com.example.timeselfie.utils.export.ExportManager
import com.example.timeselfie.utils.export.ExportResult
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExportViewModelTest {

    private lateinit var viewModel: ExportViewModel
    private lateinit var mockRepository: TimeCapsuleRepository
    private lateinit var mockExportManager: ExportManager
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        mockRepository = mockk()
        mockExportManager = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCapsuleData should load capsule and entries successfully`() = runTest {
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
                dayNumber = 15,
                mood = "happy",
                imagePath = "/path/to/image.jpg",
                imageFileName = "image.jpg"
            )
        )

        coEvery { mockRepository.getActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntriesForCapsule(1L) } returns flowOf(mockEntries)

        // When
        viewModel = ExportViewModel(mockRepository, mockExportManager)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        assertEquals("Test Capsule", uiState.capsuleName)
        assertEquals(1, uiState.completedDays)
        assertEquals(30, uiState.totalDays)
        assertEquals(mockEntries, uiState.entries)
        assertTrue(uiState.canExport)
    }

    @Test
    fun `loadCapsuleData should handle repository error`() = runTest {
        // Given
        coEvery { mockRepository.getActiveCapsule() } throws Exception("Database error")

        // When
        viewModel = ExportViewModel(mockRepository, mockExportManager)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("Failed to load capsule data: Database error", uiState.error)
        assertFalse(uiState.canExport)
    }

    @Test
    fun `exportCollage should export successfully`() = runTest {
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
                dayNumber = 15,
                mood = "happy",
                imagePath = "/path/to/image.jpg",
                imageFileName = "image.jpg"
            )
        )

        val exportResult = ExportResult.Success(
            filePath = "/path/to/export.jpg",
            fileName = "export.jpg",
            imageCount = 1
        )

        coEvery { mockRepository.getActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntriesForCapsule(1L) } returns flowOf(mockEntries)
        coEvery { mockExportManager.exportCapsule(any(), any()) } returns exportResult

        viewModel = ExportViewModel(mockRepository, mockExportManager)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.exportCollage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isExporting)
        assertEquals(1f, uiState.exportProgress, 0.01f)
        assertTrue(uiState.exportSuccess)
        assertEquals("/path/to/export.jpg", uiState.exportedFilePath)
        assertEquals("export.jpg", uiState.exportedFileName)
    }

    @Test
    fun `exportCollage should handle export error`() = runTest {
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
                dayNumber = 15,
                mood = "happy",
                imagePath = "/path/to/image.jpg",
                imageFileName = "image.jpg"
            )
        )

        val exportResult = ExportResult.Error("Export failed")

        coEvery { mockRepository.getActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntriesForCapsule(1L) } returns flowOf(mockEntries)
        coEvery { mockExportManager.exportCapsule(any(), any()) } returns exportResult

        viewModel = ExportViewModel(mockRepository, mockExportManager)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.exportCollage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isExporting)
        assertEquals(0f, uiState.exportProgress, 0.01f)
        assertFalse(uiState.exportSuccess)
        assertEquals("Export failed", uiState.error)
    }

    @Test
    fun `exportCollage should not export when no entries`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )

        coEvery { mockRepository.getActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntriesForCapsule(1L) } returns flowOf(emptyList())

        viewModel = ExportViewModel(mockRepository, mockExportManager)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.exportCollage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isExporting)
        assertEquals("No entries to export", uiState.error)
        assertFalse(uiState.canExport)
    }

    @Test
    fun `saveToGallery should save successfully after export`() = runTest {
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
                dayNumber = 15,
                mood = "happy",
                imagePath = "/path/to/image.jpg",
                imageFileName = "image.jpg"
            )
        )

        val exportResult = ExportResult.Success(
            filePath = "/path/to/export.jpg",
            fileName = "export.jpg",
            imageCount = 1
        )

        coEvery { mockRepository.getActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntriesForCapsule(1L) } returns flowOf(mockEntries)
        coEvery { mockExportManager.exportCapsule(any(), any()) } returns exportResult
        coEvery { mockExportManager.saveToGallery(any()) } returns true

        viewModel = ExportViewModel(mockRepository, mockExportManager)
        testDispatcher.scheduler.advanceUntilIdle()

        // First export to set the file path
        viewModel.exportCollage()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.saveToGallery()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.savedToGallery)
        assertEquals("Collage saved to gallery!", uiState.message)
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )

        coEvery { mockRepository.getActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntriesForCapsule(1L) } returns flowOf(emptyList())

        viewModel = ExportViewModel(mockRepository, mockExportManager)
        testDispatcher.scheduler.advanceUntilIdle()

        // Set error state by calling a method that sets an error
        viewModel.exportCollage() // This will set an error since no entries

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `clearMessage should clear message state`() = runTest {
        // Given
        val mockCapsule = TimeCapsule(
            id = 1L,
            name = "Test Capsule",
            startDate = "2025-06-01",
            endDate = "2025-06-30",
            isActive = true
        )

        coEvery { mockRepository.getActiveCapsule() } returns mockCapsule
        coEvery { mockRepository.getEntriesForCapsule(1L) } returns flowOf(emptyList())

        viewModel = ExportViewModel(mockRepository, mockExportManager)
        testDispatcher.scheduler.advanceUntilIdle()

        // Set message state by calling saveToGallery after a successful export
        // First do a successful export
        val mockEntries = listOf(
            CapsuleEntry(
                id = 1L,
                capsuleId = 1L,
                date = "2025-06-15",
                dayNumber = 15,
                mood = "happy",
                imagePath = "/path/to/image.jpg",
                imageFileName = "image.jpg"
            )
        )
        val exportResult = ExportResult.Success(
            filePath = "/path/to/export.jpg",
            fileName = "export.jpg",
            imageCount = 1
        )
        coEvery { mockRepository.getEntriesForCapsule(1L) } returns flowOf(mockEntries)
        coEvery { mockExportManager.exportCapsule(any(), any()) } returns exportResult
        coEvery { mockExportManager.saveToGallery(any()) } returns true

        viewModel.exportCollage()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.saveToGallery()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearMessage()

        // Then
        assertNull(viewModel.uiState.value.message)
    }
}
