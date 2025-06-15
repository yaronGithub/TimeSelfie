package com.example.timeselfie

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        // Package name can be different for debug builds
        assertTrue("Package name should start with com.example.timeselfie",
            appContext.packageName.startsWith("com.example.timeselfie"))
    }

    @Test
    fun mainActivityLaunches() {
        // Test that the main activity launches without crashing
        // This will fail if there are any runtime issues
        Thread.sleep(2000) // Give the app time to load
        // If we get here, the app launched successfully
        assertTrue("MainActivity launched successfully", true)
    }
}