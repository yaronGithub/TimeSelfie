package com.example.timeselfie

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timeselfie.data.repository.SettingsRepository
import com.example.timeselfie.ui.screens.daily.DailyEntryScreen
import com.example.timeselfie.ui.screens.export.ExportScreen
import com.example.timeselfie.ui.screens.onboarding.OnboardingScreen
import com.example.timeselfie.ui.screens.timeline.TimelineScreen
import com.example.timeselfie.ui.theme.TimeSelfieTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeSelfieTheme {
                val navController = rememberNavController()

                // Determine start destination based on onboarding status
                var startDestination by remember { mutableStateOf<String?>(null) }

                // Load onboarding status asynchronously
                LaunchedEffect(Unit) {
                    Log.d("MainActivity", "Loading onboarding status...")
                    val isComplete = settingsRepository.isOnboardingComplete()
                    Log.d("MainActivity", "Onboarding complete: $isComplete")
                    startDestination = if (isComplete) {
                        "timeline"
                    } else {
                        "onboarding"
                    }
                    Log.d("MainActivity", "Start destination set to: $startDestination")
                }

                // Show loading until start destination is determined
                startDestination?.let { destination ->
                    NavHost(navController = navController, startDestination = destination) {
                    composable("onboarding") {
                        OnboardingScreen(
                                onNavigateToTimeline = {
                                    Log.d("MainActivity", "onNavigateToTimeline called")
                                    navController.navigate("timeline") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                    Log.d("MainActivity", "Navigation to timeline completed")
                                }
                        )
                    }
                    composable("timeline") {
                        TimelineScreen(
                                onNavigateToDaily = { navController.navigate("daily_entry") },
                                onNavigateToExport = { navController.navigate("export") }
                        )
                    }

                    composable("daily_entry") {
                        DailyEntryScreen(onNavigateBack = { navController.popBackStack() })
                    }

                    composable("export") {
                        ExportScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimelinePreview() {
    TimeSelfieTheme { TimelineScreen(onNavigateToDaily = {}, onNavigateToExport = {}) }
}
