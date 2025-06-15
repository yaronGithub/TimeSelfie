package com.example.timeselfie.data.repository

import com.example.timeselfie.data.database.dao.AppSettingsDao
import com.example.timeselfie.data.database.entities.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing app settings and preferences.
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val appSettingsDao: AppSettingsDao
) {
    
    companion object {
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
    }
    
    /**
     * Check if onboarding has been completed.
     */
    suspend fun isOnboardingComplete(): Boolean = withContext(Dispatchers.IO) {
        val setting = appSettingsDao.getSetting(KEY_ONBOARDING_COMPLETE)
        setting?.value?.toBoolean() ?: false
    }
    
    /**
     * Mark onboarding as complete.
     */
    suspend fun setOnboardingComplete(complete: Boolean) = withContext(Dispatchers.IO) {
        appSettingsDao.setSetting(
            AppSettings(
                key = KEY_ONBOARDING_COMPLETE,
                value = complete.toString()
            )
        )
    }
    
    /**
     * Check if this is the first app launch.
     */
    suspend fun isFirstLaunch(): Boolean = withContext(Dispatchers.IO) {
        val setting = appSettingsDao.getSetting(KEY_FIRST_LAUNCH)
        if (setting == null) {
            // Mark as not first launch anymore
            appSettingsDao.setSetting(
                AppSettings(
                    key = KEY_FIRST_LAUNCH,
                    value = "false"
                )
            )
            true
        } else {
            false
        }
    }
    
    /**
     * Get theme mode preference.
     */
    suspend fun getThemeMode(): String = withContext(Dispatchers.IO) {
        val setting = appSettingsDao.getSetting(KEY_THEME_MODE)
        setting?.value ?: "system" // Default to system theme
    }
    
    /**
     * Set theme mode preference.
     */
    suspend fun setThemeMode(mode: String) = withContext(Dispatchers.IO) {
        appSettingsDao.setSetting(
            AppSettings(
                key = KEY_THEME_MODE,
                value = mode
            )
        )
    }
    
    /**
     * Check if notifications are enabled.
     */
    suspend fun isNotificationEnabled(): Boolean = withContext(Dispatchers.IO) {
        val setting = appSettingsDao.getSetting(KEY_NOTIFICATION_ENABLED)
        setting?.value?.toBoolean() ?: true // Default to enabled
    }
    
    /**
     * Set notification preference.
     */
    suspend fun setNotificationEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        appSettingsDao.setSetting(
            AppSettings(
                key = KEY_NOTIFICATION_ENABLED,
                value = enabled.toString()
            )
        )
    }
    
    /**
     * Clear all settings (for testing or reset purposes).
     */
    suspend fun clearAllSettings() = withContext(Dispatchers.IO) {
        appSettingsDao.deleteSetting(KEY_ONBOARDING_COMPLETE)
        appSettingsDao.deleteSetting(KEY_FIRST_LAUNCH)
        appSettingsDao.deleteSetting(KEY_THEME_MODE)
        appSettingsDao.deleteSetting(KEY_NOTIFICATION_ENABLED)
    }
    
    /**
     * Get a custom setting value.
     */
    suspend fun getCustomSetting(key: String): String? = withContext(Dispatchers.IO) {
        appSettingsDao.getSetting(key)?.value
    }
    
    /**
     * Set a custom setting value.
     */
    suspend fun setCustomSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        appSettingsDao.setSetting(
            AppSettings(
                key = key,
                value = value
            )
        )
    }
}
