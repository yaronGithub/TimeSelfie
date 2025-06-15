package com.example.timeselfie.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a mood entry for a specific date.
 * This is a simplified version for backward compatibility.
 */
@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey
    val date: String, // Format: "2025-06-15"
    val mood: String,
    val imagePath: String,
    val imageFileName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
