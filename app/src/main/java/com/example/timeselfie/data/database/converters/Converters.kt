package com.example.timeselfie.data.database.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters for Room database.
 * Handles conversion between complex types and primitive types that Room can persist.
 */
class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
