package com.example.timeselfie.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.timeselfie.data.database.converters.Converters
import com.example.timeselfie.data.database.dao.AppSettingsDao
import com.example.timeselfie.data.database.dao.CapsuleEntryDao
import com.example.timeselfie.data.database.dao.MoodEntryDao
import com.example.timeselfie.data.database.dao.TimeCapsuleDao
import com.example.timeselfie.data.database.entities.AppSettings
import com.example.timeselfie.data.database.entities.CapsuleEntry
import com.example.timeselfie.data.database.entities.MoodEntry
import com.example.timeselfie.data.database.entities.TimeCapsule

/**
 * Main database class for the Time Selfie app.
 * Contains all entities and provides access to DAOs.
 */
@Database(
    entities = [
        MoodEntry::class,
        TimeCapsule::class,
        CapsuleEntry::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TimeSelfieDatabase : RoomDatabase() {
    
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun timeCapsuleDao(): TimeCapsuleDao
    abstract fun capsuleEntryDao(): CapsuleEntryDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: TimeSelfieDatabase? = null

        fun getDatabase(context: Context): TimeSelfieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeSelfieDatabase::class.java,
                    "time_selfie_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
