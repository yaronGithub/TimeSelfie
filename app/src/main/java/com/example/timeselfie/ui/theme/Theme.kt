package com.example.timeselfie.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
        darkColorScheme(
                primary = Primary,
                secondary = Secondary,
                tertiary = PastelPink,
                background = BackgroundDark,
                surface = SurfaceDark,
                onPrimary = OnPrimaryDark,
                onSecondary = OnSecondaryDark,
                onBackground = OnBackgroundDark,
                onSurface = OnSurfaceDark,
                error = ErrorDark,
                onError = OnErrorDark
        )

private val LightColorScheme =
        lightColorScheme(
                primary = Primary,
                secondary = Secondary,
                tertiary = PastelPink,
                background = BackgroundLight,
                surface = SurfaceLight,
                onPrimary = OnPrimaryLight,
                onSecondary = OnSecondaryLight,
                onBackground = OnBackgroundLight,
                onSurface = OnSurfaceLight,
                error = ErrorLight,
                onError = OnErrorLight
        )

@Composable
fun TimeSelfieTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        // Dynamic color is available on Android 12+
        dynamicColor: Boolean = true,
        content: @Composable () -> Unit
) {
    val colorScheme =
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context)
                    else dynamicLightColorScheme(context)
                }
                darkTheme -> DarkColorScheme
                else -> LightColorScheme
            }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
