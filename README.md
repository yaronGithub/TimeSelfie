# Time Capsule Selfies 📸

A minimalist, emotional, and personal Android app that helps users build a 30-day collage of selfies, each paired with a one-word mood. Capture daily moments and compile them into a visual time capsule you can export and share.

## ✨ Features

### 📱 Core Functionality
- **Daily Selfie Capture**: Take a selfie each day using camera or gallery
- **Mood Tracking**: Pair each selfie with a one-word mood description
- **30-Day Timeline**: Beautiful grid view showing your complete journey
- **Collage Export**: Generate and share stunning collages of your time capsule
- **Local Storage**: All data stored securely on your device

### 🎨 Design & UX
- **Material Design 3**: Modern, clean interface with pastel color scheme
- **Intuitive Navigation**: Smooth transitions between screens
- **Responsive Layout**: Optimized for all Android screen sizes
- **Accessibility**: Full support for screen readers and accessibility features

### 🔒 Privacy & Security
- **100% Local**: No data leaves your device
- **No Account Required**: Start using immediately
- **Secure Storage**: Images and data encrypted on device
- **Permission Control**: Only requests necessary permissions

## 🚀 Getting Started

### Prerequisites
- Android 7.0 (API level 24) or higher
- Camera permission for taking selfies
- Storage permission for saving images

### Installation
1. Download the APK from the releases page
2. Enable "Install from unknown sources" if needed
3. Install and grant required permissions
4. Start your 30-day journey!

## 🏗️ Technical Architecture

### Built With
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material Design 3** - Design system
- **Room Database** - Local data persistence
- **CameraX** - Camera functionality
- **Hilt** - Dependency injection
- **Coroutines** - Asynchronous programming
- **Coil** - Image loading and caching

### Architecture Pattern
- **MVVM** - Model-View-ViewModel pattern
- **Clean Architecture** - Separation of concerns
- **Repository Pattern** - Data access abstraction
- **Use Cases** - Business logic encapsulation

### Performance Optimizations
- **Image Compression** - Automatic image optimization
- **Thumbnail Generation** - Fast grid loading
- **Memory Management** - Efficient bitmap handling
- **Database Optimization** - WAL mode and indexing
- **Lazy Loading** - On-demand content loading

## 📁 Project Structure

```
app/
├── src/main/java/com/example/timeselfie/
│   ├── data/                    # Data layer
│   │   ├── database/           # Room database
│   │   ├── repository/         # Data repositories
│   │   └── models/            # Data models
│   ├── ui/                     # UI layer
│   │   ├── screens/           # Screen composables
│   │   ├── components/        # Reusable UI components
│   │   └── theme/            # App theming
│   ├── utils/                  # Utility classes
│   │   ├── camera/           # Camera utilities
│   │   ├── image/            # Image processing
│   │   ├── storage/          # File management
│   │   └── date/             # Date utilities
│   └── di/                    # Dependency injection
└── src/test/                  # Unit tests
```

## 🧪 Testing

The app includes comprehensive testing:

- **Unit Tests** - ViewModels and repositories
- **Integration Tests** - Database operations
- **UI Tests** - Critical user flows

Run tests:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## 🔧 Development

### Setup
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run on device or emulator

### Build Variants
- **Debug** - Development build with logging
- **Release** - Optimized production build

### Code Quality
- **Kotlin Coding Conventions** - Standard formatting
- **Detekt** - Static code analysis
- **Unit Test Coverage** - Comprehensive testing
- **ProGuard** - Code obfuscation and optimization

## 📱 App Store Information

### Short Description
Create beautiful 30-day selfie collages with mood tracking. Capture your journey, one day at a time.

### Full Description
Transform your daily moments into beautiful memories with Time Capsule Selfies. This minimalist app helps you create a 30-day visual journey by capturing a selfie and mood each day, then compiling them into stunning collages you can share with friends and family.

**Key Features:**
• Daily selfie capture with camera or gallery
• One-word mood tracking for emotional context
• Beautiful 30-day timeline grid view
• Export collages as high-quality images
• Share your time capsule with others
• 100% private - all data stays on your device
• No accounts or sign-ups required

**Perfect for:**
• Personal growth and reflection
• Documenting life changes
• Creating unique social media content
• Building healthy daily habits
• Capturing special 30-day challenges

Start your visual journey today and see how you change over time!

### Keywords
selfie, mood tracker, photo collage, daily habits, personal growth, timeline, visual diary, 30-day challenge, memory keeper, photo journal

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Support

For support, please open an issue on GitHub or contact [your-email@example.com].

## 🎯 Roadmap

- [ ] Widget for home screen
- [ ] Multiple time capsule support
- [ ] Cloud backup option
- [ ] Advanced collage layouts
- [ ] Mood analytics and insights
- [ ] Social sharing improvements

---

**Made with ❤️ for capturing life's beautiful moments**
