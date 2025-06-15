package com.example.timeselfie.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeselfie.data.models.TimelineItem
import com.example.timeselfie.data.models.TimelineState
import com.example.timeselfie.data.repository.TimeCapsuleRepository
import com.example.timeselfie.utils.date.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Timeline screen.
 * Manages the state of the 30-day grid view.
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val timeCapsuleRepository: TimeCapsuleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineState())
    val uiState: StateFlow<TimelineState> = _uiState.asStateFlow()

    init {
        loadTimeline()
    }

    private fun loadTimeline() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val activeCapsule = timeCapsuleRepository.getOrCreateActiveCapsule()
                
                // Get all entries for the active capsule
                timeCapsuleRepository.getEntriesForCapsule(activeCapsule.id)
                    .combine(
                        // Create a flow of the next 30 days
                        kotlinx.coroutines.flow.flowOf(DateUtils.getNext30Days())
                    ) { entries, dates ->
                        val entryMap = entries.associateBy { it.date }
                        
                        dates.mapIndexed { index, date ->
                            val entry = entryMap[date]
                            TimelineItem(
                                date = date,
                                dayNumber = index + 1,
                                mood = entry?.mood,
                                imagePath = entry?.imagePath,
                                thumbnailPath = entry?.thumbnailPath,
                                isEmpty = entry == null
                            )
                        }
                    }
                    .collect { timelineItems ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            entries = timelineItems,
                            currentCapsule = activeCapsule.name
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun refreshTimeline() {
        loadTimeline()
    }
}
