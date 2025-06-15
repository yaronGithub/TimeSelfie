# Time Capsule Selfies — App Specification

A minimalist, emotional, and personal Android app that helps users build a 30-day collage of selfies, each paired with a one-word mood. The app captures daily moments and compiles them into a visual time capsule the user can export and share.

## 🧭 App Flow Overview

### Welcome / Onboarding

- Brief explanation of the app's concept
- Permissions request (camera, storage)
- Start button

### Daily Entry Flow

- Camera or Gallery option
- One-word mood input
- Save selfie + mood for current day

### Timeline View (Main Screen)

- 30-day grid showing all past entries
- Each square: thumbnail + mood word overlay
- Tap to view full-size selfie + mood

### Export Collage Screen

- Preview full 30-day grid
- Option to export as a single image (collage)
- Share or save locally

## 🧩 Features & Components

### 1. 📆 Daily Entry Screen

**UI Elements:**

- Open camera or choose from gallery
- Input field for one-word mood
- "Save Day" button

**Functionality:**

- Image + mood word is saved with the current date
- One entry per day enforced

### 2. 🗂️ Timeline Grid View

**UI Elements:**

- RecyclerView or GridLayout (6 rows × 5 columns)
- Each cell: selfie thumbnail + mood word (text overlay)

**Functionality:**

- Tapping opens a modal/fullscreen view:
  - Full-size selfie
  - Mood word
  - Date
  - Option to delete or edit

### 3. 🖼️ Collage Export

**UI Elements:**

- "Export My Time Capsule" button
- Preview of 30-day collage
- Save / Share buttons

**Functionality:**

- Combine saved selfies and mood words into a single image
- Export as .jpg or .png to gallery
- Optional: add user's name and title (e.g. "May 2025")

## 🗄️ Data Storage

### Selfies

- Saved in internal storage (e.g. `/data/user/0/.../selfies/YYYY-MM-DD.jpg`)

### Mood Data

- JSON file or Room database with schema:

```kotlin
data class MoodEntry(
  val date: String, // "2025-06-15"
  val mood: String,
  val imagePath: String
)
```

### Persistence

- On app open, read all entries and generate the 30-day grid
- Only show dates with saved data

## 🔐 Permissions Needed

- Camera access
- Storage access (internal, for image saving)
- Optional: gallery access

## 🧪 Edge Cases

- If no entry for today → show "Add Today" button prominently
- If all 30 days are filled → allow new month reset or archive
- Allow users to edit mood word or delete entry

## 🖌️ Design Style

- Minimalist & calming UI
- Pastel colors, smooth animations
- Fonts: Sans-serif, modern
- Emphasis on user content (photos and words)

## 🚫 What's Not Included

- No AI processing
- No backend or login
- No multiplayer or social feed
- All data is stored locally

## 📱 Technical Implementation Notes

### Architecture

- MVVM pattern with Android Architecture Components
- Room database for local data persistence
- CameraX for camera functionality
- Jetpack Compose for UI
- Coil for image loading and caching

### Key Dependencies

- AndroidX Camera
- Room Database
- Jetpack Compose
- Material Design 3 Components
- Coil for image loading
- WorkManager for background tasks

## 🗃️ Complete Database Schema

### Room Database Entities

```kotlin
@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey
    val date: String, // Format: "2025-06-15"
    val mood: String,
    val imagePath: String,
    val imageFileName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "time_capsules")
data class TimeCapsule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g., "June 2025"
    val startDate: String, // "2025-06-01"
    val endDate: String, // "2025-06-30"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val exportedAt: Long? = null,
    val exportPath: String? = null
)

@Entity(
    tableName = "capsule_entries",
    foreignKeys = [
        ForeignKey(
            entity = TimeCapsule::class,
            parentColumns = ["id"],
            childColumns = ["capsuleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["capsuleId", "date"], unique = true)]
)
data class CapsuleEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val capsuleId: Long,
    val date: String, // "2025-06-15"
    val dayNumber: Int, // 1-30
    val mood: String,
    val imagePath: String,
    val imageFileName: String,
    val thumbnailPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)
```

### Data Access Objects (DAOs)

```kotlin
@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE date = :date")
    suspend fun getEntryByDate(date: String): MoodEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MoodEntry)

    @Update
    suspend fun updateEntry(entry: MoodEntry)

    @Delete
    suspend fun deleteEntry(entry: MoodEntry)

    @Query("DELETE FROM mood_entries WHERE date = :date")
    suspend fun deleteEntryByDate(date: String)

    @Query("SELECT COUNT(*) FROM mood_entries")
    suspend fun getEntryCount(): Int
}

@Dao
interface TimeCapsuleDao {
    @Query("SELECT * FROM time_capsules ORDER BY createdAt DESC")
    fun getAllCapsules(): Flow<List<TimeCapsule>>

    @Query("SELECT * FROM time_capsules WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveCapsule(): TimeCapsule?

    @Insert
    suspend fun insertCapsule(capsule: TimeCapsule): Long

    @Update
    suspend fun updateCapsule(capsule: TimeCapsule)

    @Query("UPDATE time_capsules SET isActive = 0 WHERE id != :activeId")
    suspend fun deactivateOtherCapsules(activeId: Long)
}

@Dao
interface CapsuleEntryDao {
    @Query("SELECT * FROM capsule_entries WHERE capsuleId = :capsuleId ORDER BY dayNumber ASC")
    fun getEntriesForCapsule(capsuleId: Long): Flow<List<CapsuleEntry>>

    @Query("SELECT * FROM capsule_entries WHERE capsuleId = :capsuleId AND date = :date")
    suspend fun getEntryByDate(capsuleId: Long, date: String): CapsuleEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CapsuleEntry)

    @Update
    suspend fun updateEntry(entry: CapsuleEntry)

    @Delete
    suspend fun deleteEntry(entry: CapsuleEntry)

    @Query("SELECT COUNT(*) FROM capsule_entries WHERE capsuleId = :capsuleId")
    suspend fun getEntryCountForCapsule(capsuleId: Long): Int
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun getSetting(key: String): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettings)

    @Query("DELETE FROM app_settings WHERE key = :key")
    suspend fun deleteSetting(key: String)
}
```

### Database Class

```kotlin
@Database(
    entities = [
        MoodEntry::class,
        TimeCapsule::class,
        CapsuleEntry::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TimeSelfieDatabase : RoomDatabase() {
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun timeCapsuleDao(): TimeCapsuleDao
    abstract fun capsuleEntryDao(): CapsuleEntryDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: TimeSelfieDatabase? = null

        fun getDatabase(context: Context): TimeSelfieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeSelfieDatabase::class.java,
                    "time_selfie_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

## 📁 Optimal Folder Structure

### Complete Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/timeselfie/
│   │   │   ├── TimeSelfieApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   │
│   │   │   ├── data/
│   │   │   │   ├── database/
│   │   │   │   │   ├── TimeSelfieDatabase.kt
│   │   │   │   │   ├── entities/
│   │   │   │   │   │   ├── MoodEntry.kt
│   │   │   │   │   │   ├── TimeCapsule.kt
│   │   │   │   │   │   ├── CapsuleEntry.kt
│   │   │   │   │   │   └── AppSettings.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── MoodEntryDao.kt
│   │   │   │   │   │   ├── TimeCapsuleDao.kt
│   │   │   │   │   │   ├── CapsuleEntryDao.kt
│   │   │   │   │   │   └── AppSettingsDao.kt
│   │   │   │   │   └── converters/
│   │   │   │   │       └── Converters.kt
│   │   │   │   │
│   │   │   │   ├── repository/
│   │   │   │   │   ├── TimeCapsuleRepository.kt
│   │   │   │   │   ├── MoodEntryRepository.kt
│   │   │   │   │   ├── ImageRepository.kt
│   │   │   │   │   └── SettingsRepository.kt
│   │   │   │   │
│   │   │   │   ├── models/
│   │   │   │   │   ├── UiState.kt
│   │   │   │   │   ├── CapsuleState.kt
│   │   │   │   │   ├── ExportState.kt
│   │   │   │   │   └── CameraState.kt
│   │   │   │   │
│   │   │   │   └── preferences/
│   │   │   │       └── UserPreferences.kt
│   │   │   │
│   │   │   ├── domain/
│   │   │   │   ├── usecase/
│   │   │   │   │   ├── CreateDailyEntryUseCase.kt
│   │   │   │   │   ├── GetTimelineUseCase.kt
│   │   │   │   │   ├── ExportCollageUseCase.kt
│   │   │   │   │   ├── ManageCapsuleUseCase.kt
│   │   │   │   │   └── ValidateEntryUseCase.kt
│   │   │   │   │
│   │   │   │   └── model/
│   │   │   │       ├── DailyEntry.kt
│   │   │   │       ├── TimelineItem.kt
│   │   │   │       └── CollageConfig.kt
│   │   │   │
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   ├── Type.kt
│   │   │   │   │   └── Dimension.kt
│   │   │   │   │
│   │   │   │   ├── components/
│   │   │   │   │   ├── common/
│   │   │   │   │   │   ├── LoadingIndicator.kt
│   │   │   │   │   │   ├── ErrorMessage.kt
│   │   │   │   │   │   ├── ConfirmDialog.kt
│   │   │   │   │   │   └── CustomButton.kt
│   │   │   │   │   │
│   │   │   │   │   ├── camera/
│   │   │   │   │   │   ├── CameraPreview.kt
│   │   │   │   │   │   ├── CameraControls.kt
│   │   │   │   │   │   └── GalleryPicker.kt
│   │   │   │   │   │
│   │   │   │   │   ├── timeline/
│   │   │   │   │   │   ├── TimelineGrid.kt
│   │   │   │   │   │   ├── TimelineItem.kt
│   │   │   │   │   │   ├── EmptyDayItem.kt
│   │   │   │   │   │   └── MoodOverlay.kt
│   │   │   │   │   │
│   │   │   │   │   └── export/
│   │   │   │   │       ├── CollagePreview.kt
│   │   │   │   │       ├── ExportOptions.kt
│   │   │   │   │       └── ShareDialog.kt
│   │   │   │   │
│   │   │   │   ├── screens/
│   │   │   │   │   ├── onboarding/
│   │   │   │   │   │   ├── OnboardingScreen.kt
│   │   │   │   │   │   ├── OnboardingViewModel.kt
│   │   │   │   │   │   └── PermissionScreen.kt
│   │   │   │   │   │
│   │   │   │   │   ├── daily/
│   │   │   │   │   │   ├── DailyEntryScreen.kt
│   │   │   │   │   │   ├── DailyEntryViewModel.kt
│   │   │   │   │   │   ├── CameraScreen.kt
│   │   │   │   │   │   └── MoodInputScreen.kt
│   │   │   │   │   │
│   │   │   │   │   ├── timeline/
│   │   │   │   │   │   ├── TimelineScreen.kt
│   │   │   │   │   │   ├── TimelineViewModel.kt
│   │   │   │   │   │   ├── DetailScreen.kt
│   │   │   │   │   │   └── DetailViewModel.kt
│   │   │   │   │   │
│   │   │   │   │   ├── export/
│   │   │   │   │   │   ├── ExportScreen.kt
│   │   │   │   │   │   ├── ExportViewModel.kt
│   │   │   │   │   │   └── CollagePreviewScreen.kt
│   │   │   │   │   │
│   │   │   │   │   └── settings/
│   │   │   │   │       ├── SettingsScreen.kt
│   │   │   │   │       └── SettingsViewModel.kt
│   │   │   │   │
│   │   │   │   └── navigation/
│   │   │   │       ├── TimeSelfieNavigation.kt
│   │   │   │       ├── NavigationDestinations.kt
│   │   │   │       └── NavigationArgs.kt
│   │   │   │
│   │   │   ├── utils/
│   │   │   │   ├── camera/
│   │   │   │   │   ├── CameraManager.kt
│   │   │   │   │   ├── ImageCapture.kt
│   │   │   │   │   └── CameraPermissions.kt
│   │   │   │   │
│   │   │   │   ├── image/
│   │   │   │   │   ├── ImageProcessor.kt
│   │   │   │   │   ├── ImageCompressor.kt
│   │   │   │   │   ├── ThumbnailGenerator.kt
│   │   │   │   │   └── CollageGenerator.kt
│   │   │   │   │
│   │   │   │   ├── storage/
│   │   │   │   │   ├── FileManager.kt
│   │   │   │   │   ├── ImageStorage.kt
│   │   │   │   │   └── ExportManager.kt
│   │   │   │   │
│   │   │   │   ├── date/
│   │   │   │   │   ├── DateUtils.kt
│   │   │   │   │   └── CalendarHelper.kt
│   │   │   │   │
│   │   │   │   └── extensions/
│   │   │   │       ├── ContextExtensions.kt
│   │   │   │       ├── BitmapExtensions.kt
│   │   │   │       └── StringExtensions.kt
│   │   │   │
│   │   │   └── di/
│   │   │       ├── DatabaseModule.kt
│   │   │       ├── RepositoryModule.kt
│   │   │       ├── UseCaseModule.kt
│   │   │       └── UtilsModule.kt
│   │   │
│   │   ├── res/
│   │   │   ├── drawable/
│   │   │   │   ├── ic_camera.xml
│   │   │   │   ├── ic_gallery.xml
│   │   │   │   ├── ic_export.xml
│   │   │   │   ├── ic_mood_happy.xml
│   │   │   │   ├── ic_mood_sad.xml
│   │   │   │   └── background_gradient.xml
│   │   │   │
│   │   │   ├── values/
│   │   │   │   ├── colors.xml
│   │   │   │   ├── strings.xml
│   │   │   │   ├── themes.xml
│   │   │   │   ├── dimens.xml
│   │   │   │   └── styles.xml
│   │   │   │
│   │   │   ├── values-night/
│   │   │   │   ├── colors.xml
│   │   │   │   └── themes.xml
│   │   │   │
│   │   │   ├── xml/
│   │   │   │   ├── file_paths.xml
│   │   │   │   └── backup_rules.xml
│   │   │   │
│   │   │   └── raw/
│   │   │       └── mood_suggestions.json
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   ├── test/
│   │   └── java/com/example/timeselfie/
│   │       ├── repository/
│   │       │   ├── TimeCapsuleRepositoryTest.kt
│   │       │   └── MoodEntryRepositoryTest.kt
│   │       │
│   │       ├── usecase/
│   │       │   ├── CreateDailyEntryUseCaseTest.kt
│   │       │   └── ExportCollageUseCaseTest.kt
│   │       │
│   │       └── utils/
│   │           ├── DateUtilsTest.kt
│   │           └── ImageProcessorTest.kt
│   │
│   └── androidTest/
│       └── java/com/example/timeselfie/
│           ├── database/
│           │   └── TimeSelfieeDatabaseTest.kt
│           │
│           ├── ui/
│           │   ├── TimelineScreenTest.kt
│           │   └── DailyEntryScreenTest.kt
│           │
│           └── ExampleInstrumentedTest.kt
│
├── build.gradle.kts
└── proguard-rules.pro
```

### Key Architectural Decisions

1. **Clean Architecture**: Separation of concerns with data, domain, and UI layers
2. **MVVM Pattern**: ViewModels handle UI logic and state management
3. **Repository Pattern**: Centralized data access with caching strategies
4. **Use Cases**: Business logic encapsulation for complex operations
5. **Dependency Injection**: Modular and testable code structure
6. **Compose Navigation**: Type-safe navigation with arguments

### Storage Structure

```
Internal Storage:
/data/data/com.example.timeselfie/
├── databases/
│   └── time_selfie_database
├── files/
│   ├── selfies/
│   │   ├── 2025-06-15.jpg
│   │   ├── 2025-06-16.jpg
│   │   └── ...
│   ├── thumbnails/
│   │   ├── thumb_2025-06-15.jpg
│   │   ├── thumb_2025-06-16.jpg
│   │   └── ...
│   └── exports/
│       ├── time_capsule_june_2025.jpg
│       └── ...
└── shared_prefs/
    └── user_preferences.xml
```

### Performance Considerations

- Lazy loading for timeline grid
- Image compression for storage efficiency
- Background processing for collage generation
- Efficient bitmap handling to prevent OOM errors
- Thumbnail generation for faster grid loading
- Database indexing for quick date-based queries
- Coroutines for asynchronous operations
- StateFlow for reactive UI updates
- Memory-efficient image loading with Coil
- Proper lifecycle management for camera resources

## 📋 Current Implementation Status

### ✅ Completed Features

1. **Project Setup & Architecture**

   - MVVM architecture with Jetpack Compose
   - Room database with proper entities and DAOs
   - Hilt dependency injection
   - Material Design 3 theming

2. **Database Schema**

   - MoodEntry, TimeCapsule, CapsuleEntry, AppSettings entities
   - Complete DAO interfaces with CRUD operations
   - Database migrations and type converters

3. **Core UI Components**

   - Pastel color theme with light/dark mode support
   - Custom typography and spacing
   - Reusable UI components

4. **Daily Entry Screen**

   - Camera integration with CameraX
   - Gallery picker functionality
   - Mood input with validation
   - Image processing and storage

5. **Timeline Screen**

   - 30-day grid layout with RecyclerView
   - Thumbnail generation and caching
   - Mood overlay on images
   - Empty state handling

6. **Mood Tracking**

   - One-word mood input validation
   - Mood persistence with entries
   - Mood display in timeline

7. **Export Functionality**

   - Collage generation from 30-day entries
   - Export to gallery
   - Share functionality
   - Preview before export

8. **Onboarding & First-Time Experience**
   - Welcome screen with app explanation
   - Permissions request (camera, storage)
   - Settings repository for onboarding state
   - Navigation flow from onboarding to main app

### 🔄 Next Steps

- Testing and bug fixes
- Performance optimization
- UI polish and animations
- Edge case handling
