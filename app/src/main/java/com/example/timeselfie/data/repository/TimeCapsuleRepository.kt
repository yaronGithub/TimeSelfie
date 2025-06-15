package com.example.timeselfie.data.repository

import com.example.timeselfie.data.database.dao.CapsuleEntryDao
import com.example.timeselfie.data.database.dao.TimeCapsuleDao
import com.example.timeselfie.data.database.entities.CapsuleEntry
import com.example.timeselfie.data.database.entities.TimeCapsule
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing time capsules and their entries.
 * Provides a clean API for the UI layer to interact with capsule data.
 */
@Singleton
class TimeCapsuleRepository @Inject constructor(
    private val timeCapsuleDao: TimeCapsuleDao,
    private val capsuleEntryDao: CapsuleEntryDao
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    fun getAllCapsules(): Flow<List<TimeCapsule>> = timeCapsuleDao.getAllCapsules()
    
    suspend fun getActiveCapsule(): TimeCapsule? = timeCapsuleDao.getActiveCapsule()
    
    suspend fun createNewCapsule(name: String): Long {
        val today = Calendar.getInstance()
        val startDate = dateFormat.format(today.time)
        
        // Calculate end date (30 days from start)
        today.add(Calendar.DAY_OF_MONTH, 29)
        val endDate = dateFormat.format(today.time)
        
        val capsule = TimeCapsule(
            name = name,
            startDate = startDate,
            endDate = endDate,
            isActive = true
        )
        
        // Deactivate other capsules first
        timeCapsuleDao.deactivateAllCapsules()
        
        return timeCapsuleDao.insertCapsule(capsule)
    }
    
    suspend fun getOrCreateActiveCapsule(): TimeCapsule {
        return getActiveCapsule() ?: run {
            val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
            val capsuleId = createNewCapsule(currentMonth)
            timeCapsuleDao.getCapsuleById(capsuleId)!!
        }
    }
    
    fun getEntriesForCapsule(capsuleId: Long): Flow<List<CapsuleEntry>> = 
        capsuleEntryDao.getEntriesForCapsule(capsuleId)
    
    suspend fun addEntryToCapsule(
        capsuleId: Long,
        date: String,
        mood: String,
        imagePath: String,
        imageFileName: String
    ) {
        val dayNumber = calculateDayNumber(capsuleId, date)
        val entry = CapsuleEntry(
            capsuleId = capsuleId,
            date = date,
            dayNumber = dayNumber,
            mood = mood,
            imagePath = imagePath,
            imageFileName = imageFileName
        )
        capsuleEntryDao.insertEntry(entry)
    }
    
    private suspend fun calculateDayNumber(capsuleId: Long, date: String): Int {
        val maxDay = capsuleEntryDao.getMaxDayNumberForCapsule(capsuleId) ?: 0
        return maxDay + 1
    }
    
    suspend fun updateEntry(entry: CapsuleEntry) = capsuleEntryDao.updateEntry(entry)
    
    suspend fun deleteEntry(entry: CapsuleEntry) = capsuleEntryDao.deleteEntry(entry)
    
    suspend fun getEntryByDate(capsuleId: Long, date: String): CapsuleEntry? = 
        capsuleEntryDao.getEntryByDate(capsuleId, date)
}
