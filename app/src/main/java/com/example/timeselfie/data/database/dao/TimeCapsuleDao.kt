package com.example.timeselfie.data.database.dao

import androidx.room.*
import com.example.timeselfie.data.database.entities.TimeCapsule
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TimeCapsule operations.
 * Manages time capsule creation, retrieval, and updates.
 */
@Dao
interface TimeCapsuleDao {
    
    @Query("SELECT * FROM time_capsules ORDER BY createdAt DESC")
    fun getAllCapsules(): Flow<List<TimeCapsule>>

    @Query("SELECT * FROM time_capsules WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveCapsule(): TimeCapsule?

    @Query("SELECT * FROM time_capsules WHERE id = :id")
    suspend fun getCapsuleById(id: Long): TimeCapsule?

    @Insert
    suspend fun insertCapsule(capsule: TimeCapsule): Long

    @Update
    suspend fun updateCapsule(capsule: TimeCapsule)

    @Delete
    suspend fun deleteCapsule(capsule: TimeCapsule)

    @Query("UPDATE time_capsules SET isActive = 0 WHERE id != :activeId")
    suspend fun deactivateOtherCapsules(activeId: Long)
    
    @Query("UPDATE time_capsules SET isActive = 0")
    suspend fun deactivateAllCapsules()
}
