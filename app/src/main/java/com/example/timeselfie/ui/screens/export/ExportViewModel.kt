package com.example.timeselfie.ui.screens.export

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeselfie.data.database.entities.CapsuleEntry
import com.example.timeselfie.data.repository.TimeCapsuleRepository
import com.example.timeselfie.utils.export.ExportManager
import com.example.timeselfie.utils.export.ExportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for the Export screen. Handles collage generation and export operations. */
@HiltViewModel
class ExportViewModel
@Inject
constructor(
        private val timeCapsuleRepository: TimeCapsuleRepository,
        private val exportManager: ExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    init {
        loadCapsuleData()
    }

    fun loadCapsuleData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val activeCapsule = timeCapsuleRepository.getActiveCapsule()
                if (activeCapsule == null) {
                    _uiState.value =
                            _uiState.value.copy(
                                    isLoading = false,
                                    error = "No active time capsule found"
                            )
                    return@launch
                }

                // Get entries for the capsule
                timeCapsuleRepository.getEntriesForCapsule(activeCapsule.id).collect { entries ->
                    val completedEntries = entries.filter { it.imagePath.isNotEmpty() }

                    _uiState.value =
                            _uiState.value.copy(
                                    isLoading = false,
                                    capsuleName = activeCapsule.name,
                                    totalDays = 30,
                                    completedDays = completedEntries.size,
                                    entries = completedEntries,
                                    canExport = completedEntries.isNotEmpty()
                            )
                }
            } catch (e: Exception) {
                _uiState.value =
                        _uiState.value.copy(
                                isLoading = false,
                                error = "Failed to load capsule data: ${e.message}"
                        )
            }
        }
    }

    fun exportCollage() {
        val currentState = _uiState.value
        if (!currentState.canExport || currentState.entries.isEmpty()) {
            _uiState.value = currentState.copy(error = "No entries to export")
            return
        }

        viewModelScope.launch {
            _uiState.value =
                    currentState.copy(isExporting = true, exportProgress = 0f, error = null)

            try {
                // Simulate progress updates
                for (progress in listOf(0.2f, 0.5f, 0.8f)) {
                    _uiState.value = _uiState.value.copy(exportProgress = progress)
                    kotlinx.coroutines.delay(500) // Simulate work
                }

                val result =
                        exportManager.exportCapsule(
                                capsuleName = currentState.capsuleName,
                                entries = currentState.entries
                        )

                when (result) {
                    is ExportResult.Success -> {
                        _uiState.value =
                                _uiState.value.copy(
                                        isExporting = false,
                                        exportProgress = 1f,
                                        exportedFilePath = result.filePath,
                                        exportedFileName = result.fileName,
                                        exportSuccess = true
                                )
                    }
                    is ExportResult.Error -> {
                        _uiState.value =
                                _uiState.value.copy(
                                        isExporting = false,
                                        exportProgress = 0f,
                                        error = result.message
                                )
                    }
                }
            } catch (e: Exception) {
                _uiState.value =
                        _uiState.value.copy(
                                isExporting = false,
                                exportProgress = 0f,
                                error = "Export failed: ${e.message}"
                        )
            }
        }
    }

    fun shareCollage(): Intent? {
        val filePath = _uiState.value.exportedFilePath
        return if (filePath != null) {
            exportManager.shareCollage(filePath)
        } else null
    }

    fun saveToGallery() {
        val filePath = _uiState.value.exportedFilePath ?: return

        viewModelScope.launch {
            try {
                val success = exportManager.saveToGallery(filePath)
                if (success) {
                    _uiState.value =
                            _uiState.value.copy(
                                    savedToGallery = true,
                                    message = "Collage saved to gallery!"
                            )
                } else {
                    _uiState.value = _uiState.value.copy(error = "Failed to save to gallery")
                }
            } catch (e: Exception) {
                _uiState.value =
                        _uiState.value.copy(error = "Failed to save to gallery: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun resetExportState() {
        _uiState.value =
                _uiState.value.copy(
                        exportSuccess = false,
                        exportedFilePath = null,
                        exportedFileName = null,
                        savedToGallery = false,
                        exportProgress = 0f
                )
    }
}

/** UI state for the Export screen. */
data class ExportUiState(
        val isLoading: Boolean = false,
        val isExporting: Boolean = false,
        val exportProgress: Float = 0f,
        val error: String? = null,
        val message: String? = null,
        val capsuleName: String = "",
        val totalDays: Int = 30,
        val completedDays: Int = 0,
        val entries: List<CapsuleEntry> = emptyList(),
        val canExport: Boolean = false,
        val exportSuccess: Boolean = false,
        val exportedFilePath: String? = null,
        val exportedFileName: String? = null,
        val savedToGallery: Boolean = false
) {
    val completionPercentage: Int
        get() = if (totalDays > 0) (completedDays * 100) / totalDays else 0
}
