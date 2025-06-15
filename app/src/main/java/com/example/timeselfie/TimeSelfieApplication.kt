package com.example.timeselfie

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the Time Selfie app.
 * Initializes Hilt for dependency injection.
 */
@HiltAndroidApp
class TimeSelfieApplication : Application()
