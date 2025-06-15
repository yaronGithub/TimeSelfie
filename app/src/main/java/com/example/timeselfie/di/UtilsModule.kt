package com.example.timeselfie.di

import android.content.Context
import com.example.timeselfie.utils.export.CollageGenerator
import com.example.timeselfie.utils.export.ExportManager
import com.example.timeselfie.utils.image.ImageProcessor
import com.example.timeselfie.utils.performance.PerformanceMonitor
import com.example.timeselfie.utils.storage.ImageStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt module for providing utility dependencies. */
@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    @Provides
    @Singleton
    fun provideImageProcessor(@ApplicationContext context: Context): ImageProcessor {
        return ImageProcessor(context)
    }

    @Provides
    @Singleton
    fun provideImageStorage(
            @ApplicationContext context: Context,
            imageProcessor: ImageProcessor
    ): ImageStorage {
        return ImageStorage(context, imageProcessor)
    }

    @Provides
    @Singleton
    fun provideCollageGenerator(@ApplicationContext context: Context): CollageGenerator {
        return CollageGenerator(context)
    }

    @Provides
    @Singleton
    fun provideExportManager(
            @ApplicationContext context: Context,
            imageStorage: ImageStorage,
            collageGenerator: CollageGenerator
    ): ExportManager {
        return ExportManager(context, imageStorage, collageGenerator)
    }
}
