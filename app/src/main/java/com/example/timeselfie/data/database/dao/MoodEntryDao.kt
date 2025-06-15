package com.example.timeselfie.data.database.dao

import androidx.room.*
import com.example.timeselfie.data.database.entities.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MoodEntry operations.
 * Provides methods for CRUD operations on mood entries.
 */
@Dao
interface MoodEntryDao {
    
    @Query("SELECT * FROM mood_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE date = :date")
    suspend fun getEntryByDate(date: String): MoodEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MoodEntry)

    @Update
    suspend fun updateEntry(entry: MoodEntry)

    @Delete
    suspend fun deleteEntry(entry: MoodEntry)

    @Query("DELETE FROM mood_entries WHERE date = :date")
    suspend fun deleteEntryByDate(date: String)

    @Query("SELECT COUNT(*) FROM mood_entries")
    suspend fun getEntryCount(): Int
    
    @Query("SELECT * FROM mood_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getEntriesInDateRange(startDate: String, endDate: String): List<MoodEntry>
}
