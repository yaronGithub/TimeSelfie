package com.example.timeselfie.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeselfie.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the onboarding screen.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    
    /**
     * Mark onboarding as complete and navigate to main app.
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            try {
                settingsRepository.setOnboardingComplete(true)
                _uiState.value = _uiState.value.copy(isOnboardingComplete = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save onboarding state: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear any error state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for the onboarding screen.
 */
data class OnboardingUiState(
    val isOnboardingComplete: Boolean = false,
    val error: String? = null
)
