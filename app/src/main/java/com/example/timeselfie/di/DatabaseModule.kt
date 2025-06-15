package com.example.timeselfie.di

import android.content.Context
import androidx.room.Room
import com.example.timeselfie.data.database.TimeSelfieDatabase
import com.example.timeselfie.data.database.dao.AppSettingsDao
import com.example.timeselfie.data.database.dao.CapsuleEntryDao
import com.example.timeselfie.data.database.dao.MoodEntryDao
import com.example.timeselfie.data.database.dao.TimeCapsuleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTimeSelfieDatabase(@ApplicationContext context: Context): TimeSelfieDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TimeSelfieDatabase::class.java,
            "time_selfie_database"
        ).build()
    }

    @Provides
    fun provideMoodEntryDao(database: TimeSelfieDatabase): MoodEntryDao {
        return database.moodEntryDao()
    }

    @Provides
    fun provideTimeCapsuleDao(database: TimeSelfieDatabase): TimeCapsuleDao {
        return database.timeCapsuleDao()
    }

    @Provides
    fun provideCapsuleEntryDao(database: TimeSelfieDatabase): CapsuleEntryDao {
        return database.capsuleEntryDao()
    }

    @Provides
    fun provideAppSettingsDao(database: TimeSelfieDatabase): AppSettingsDao {
        return database.appSettingsDao()
    }
}
