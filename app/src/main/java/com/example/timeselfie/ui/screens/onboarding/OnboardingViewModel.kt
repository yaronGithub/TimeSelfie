package com.example.timeselfie.ui.screens.onboarding

import android.util.Log
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
        Log.d("OnboardingViewModel", "completeOnboarding() called")
        viewModelScope.launch {
            try {
                Log.d("OnboardingViewModel", "Setting onboarding complete to true")
                settingsRepository.setOnboardingComplete(true)
                Log.d("OnboardingViewModel", "Updating UI state")
                _uiState.value = _uiState.value.copy(isOnboardingComplete = true)
                Log.d("OnboardingViewModel", "UI state updated: ${_uiState.value}")
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Error completing onboarding", e)
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
