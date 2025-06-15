package com.example.timeselfie.ui.screens.daily

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeselfie.data.repository.TimeCapsuleRepository
import com.example.timeselfie.utils.date.DateUtils
import com.example.timeselfie.utils.storage.ImageStorage
import com.example.timeselfie.utils.storage.SaveResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Daily Entry screen.
 * Handles camera capture, mood input, and saving entries.
 */
@HiltViewModel
class DailyEntryViewModel @Inject constructor(
    private val timeCapsuleRepository: TimeCapsuleRepository,
    private val imageStorage: ImageStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyEntryUiState())
    val uiState: StateFlow<DailyEntryUiState> = _uiState.asStateFlow()

    private val today = DateUtils.getTodayString()

    init {
        checkExistingEntry()
    }

    private fun checkExistingEntry() {
        viewModelScope.launch {
            try {
                val activeCapsule = timeCapsuleRepository.getOrCreateActiveCapsule()
                val existingEntry = timeCapsuleRepository.getEntryByDate(activeCapsule.id, today)
                
                if (existingEntry != null) {
                    _uiState.value = _uiState.value.copy(
                        hasExistingEntry = true,
                        mood = existingEntry.mood,
                        capturedImageUri = imageStorage.getSelfieUri(today)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to check existing entry: ${e.message}"
                )
            }
        }
    }

    fun onImageCaptured(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            capturedImageUri = uri,
            error = null
        )
    }

    fun onMoodChanged(mood: String) {
        _uiState.value = _uiState.value.copy(
            mood = mood.trim(),
            error = null
        )
    }

    fun onSaveEntry() {
        val currentState = _uiState.value
        
        if (currentState.capturedImageUri == null) {
            _uiState.value = currentState.copy(error = "Please take a selfie first")
            return
        }
        
        if (currentState.mood.isBlank()) {
            _uiState.value = currentState.copy(error = "Please enter your mood")
            return
        }
        
        if (currentState.mood.contains(" ")) {
            _uiState.value = currentState.copy(error = "Mood should be one word only")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            try {
                // Save the image
                val saveResult = imageStorage.saveSelfie(currentState.capturedImageUri!!, today)
                
                when (saveResult) {
                    is SaveResult.Success -> {
                        // Get or create active capsule
                        val activeCapsule = timeCapsuleRepository.getOrCreateActiveCapsule()
                        
                        // Add entry to capsule
                        timeCapsuleRepository.addEntryToCapsule(
                            capsuleId = activeCapsule.id,
                            date = today,
                            mood = currentState.mood,
                            imagePath = saveResult.imagePath,
                            imageFileName = saveResult.fileName
                        )
                        
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            isSaved = true,
                            hasExistingEntry = true
                        )
                    }
                    
                    is SaveResult.Error -> {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            error = saveResult.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = "Failed to save entry: ${e.message}"
                )
            }
        }
    }

    fun onRetakePhoto() {
        _uiState.value = _uiState.value.copy(
            capturedImageUri = null,
            error = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetSavedState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}

/**
 * UI state for the Daily Entry screen.
 */
data class DailyEntryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val capturedImageUri: Uri? = null,
    val mood: String = "",
    val isSaved: Boolean = false,
    val hasExistingEntry: Boolean = false
) {
    val canSave: Boolean
        get() = capturedImageUri != null && mood.isNotBlank() && !isLoading
}
