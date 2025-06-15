package com.example.timeselfie.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing app settings and preferences.
 */
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)
