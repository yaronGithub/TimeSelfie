package com.example.timeselfie.data.models

/**
 * Sealed class representing different UI states.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable) : UiState<Nothing>()
}

/**
 * State for camera operations.
 */
data class CameraState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val capturedImagePath: String? = null
)

/**
 * State for mood entry operations.
 */
data class MoodEntryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentMood: String = "",
    val isEntryComplete: Boolean = false
)

/**
 * State for timeline view.
 */
data class TimelineState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val entries: List<TimelineItem> = emptyList(),
    val currentCapsule: String? = null
)

/**
 * Represents an item in the timeline grid.
 */
data class TimelineItem(
    val date: String,
    val dayNumber: Int,
    val mood: String?,
    val imagePath: String?,
    val thumbnailPath: String?,
    val isEmpty: Boolean = false
)
