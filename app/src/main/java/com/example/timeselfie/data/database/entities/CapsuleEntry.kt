package com.example.timeselfie.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a single entry within a time capsule.
 * Each entry belongs to a specific capsule and represents one day.
 */
@Entity(
    tableName = "capsule_entries",
    foreignKeys = [
        ForeignKey(
            entity = TimeCapsule::class,
            parentColumns = ["id"],
            childColumns = ["capsuleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["capsuleId", "date"], unique = true)]
)
data class CapsuleEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val capsuleId: Long,
    val date: String, // "2025-06-15"
    val dayNumber: Int, // 1-30
    val mood: String,
    val imagePath: String,
    val imageFileName: String,
    val thumbnailPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
