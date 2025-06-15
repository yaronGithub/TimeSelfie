package com.example.timeselfie.data.database.dao

import androidx.room.*
import com.example.timeselfie.data.database.entities.CapsuleEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for CapsuleEntry operations.
 * Manages individual entries within time capsules.
 */
@Dao
interface CapsuleEntryDao {
    
    @Query("SELECT * FROM capsule_entries WHERE capsuleId = :capsuleId ORDER BY dayNumber ASC")
    fun getEntriesForCapsule(capsuleId: Long): Flow<List<CapsuleEntry>>

    @Query("SELECT * FROM capsule_entries WHERE capsuleId = :capsuleId AND date = :date")
    suspend fun getEntryByDate(capsuleId: Long, date: String): CapsuleEntry?

    @Query("SELECT * FROM capsule_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): CapsuleEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CapsuleEntry)

    @Update
    suspend fun updateEntry(entry: CapsuleEntry)

    @Delete
    suspend fun deleteEntry(entry: CapsuleEntry)

    @Query("DELETE FROM capsule_entries WHERE capsuleId = :capsuleId AND date = :date")
    suspend fun deleteEntryByDate(capsuleId: Long, date: String)

    @Query("SELECT COUNT(*) FROM capsule_entries WHERE capsuleId = :capsuleId")
    suspend fun getEntryCountForCapsule(capsuleId: Long): Int
    
    @Query("SELECT MAX(dayNumber) FROM capsule_entries WHERE capsuleId = :capsuleId")
    suspend fun getMaxDayNumberForCapsule(capsuleId: Long): Int?
}
