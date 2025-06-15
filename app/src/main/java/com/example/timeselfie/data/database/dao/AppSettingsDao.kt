package com.example.timeselfie.data.database.dao

import androidx.room.*
import com.example.timeselfie.data.database.entities.AppSettings

/**
 * Data Access Object for AppSettings operations.
 * Manages app configuration and user preferences.
 */
@Dao
interface AppSettingsDao {
    
    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun getSetting(key: String): AppSettings?

    @Query("SELECT * FROM app_settings")
    suspend fun getAllSettings(): List<AppSettings>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettings)

    @Query("DELETE FROM app_settings WHERE key = :key")
    suspend fun deleteSetting(key: String)
    
    @Query("DELETE FROM app_settings")
    suspend fun deleteAllSettings()
}
