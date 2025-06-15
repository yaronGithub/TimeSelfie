# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Room database classes
-keep class com.example.timeselfie.data.database.entities.** { *; }
-keep class com.example.timeselfie.data.database.dao.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Keep Coil classes
-keep class coil.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }

# Keep coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep data classes used in UI state
-keep class com.example.timeselfie.data.models.** { *; }
-keep class com.example.timeselfie.ui.screens.**.UiState { *; }
-keep class com.example.timeselfie.ui.screens.**.*UiState { *; }

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}