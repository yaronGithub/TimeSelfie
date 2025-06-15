package com.example.timeselfie.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a time capsule (30-day period).
 * Each capsule contains multiple daily entries.
 */
@Entity(tableName = "time_capsules")
data class TimeCapsule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g., "June 2025"
    val startDate: String, // "2025-06-01"
    val endDate: String, // "2025-06-30"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val exportedAt: Long? = null,
    val exportPath: String? = null
)
