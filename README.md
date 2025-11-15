# SpeedTest - Android Network Speed Test App

A professional Android network speed testing application built with **Kotlin**, **Jetpack Compose**, and **Material Design 3**.

## Features

### Core Functionality
- ✅ **Multi-threaded Speed Testing**
  - Download speed test (Mbps/MB/s)
  - Upload speed test
  - Ping/Latency measurement
  - Jitter calculation
  - Packet loss detection

- ✅ **Network Information**
  - Automatic network type detection (WiFi/4G/5G/Ethernet)
  - Network operator identification
  - External IP address display

- ✅ **Test History**
  - Local SQLite storage
  - Filter by time period (Today/Week/Month/All)
  - Filter by network type
  - Beautiful statistics and charts

- ✅ **Server Management**
  - Multiple test servers
  - Auto-select best server
  - Manual server selection
  - Server ping comparison

- ✅ **Settings & Customization**
  - Speed unit toggle (Mbps/MB/s)
  - Theme modes (Light/Dark/System)
  - Multi-language support (English/Chinese)
  - Test duration configuration
  - Thread count settings

## Technology Stack

### Architecture
- **MVVM** (Model-View-ViewModel)
- **Clean Architecture** with separation of concerns
- **Unidirectional Data Flow** (UDF)

### UI Layer
- **Jetpack Compose** 1.5+
- **Material Design 3** (Material You)
- **Compose Navigation**
- **Lifecycle-aware components**

### Data Layer
- **Room** Database for local storage
- **DataStore** for preferences
- **Retrofit** + **OkHttp** for networking
- **Kotlin Coroutines** + **Flow** for async operations

### Dependency Injection
- **Hilt** (Dagger)

### Additional Libraries
- **MPAndroidChart** for data visualization
- **Accompanist** for system UI control
- **WorkManager** for background tasks
- **Gson** for JSON serialization

## Requirements

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Kotlin**: 1.9+
- **Gradle**: 8.0+
- **Android Studio**: Hedgehog or later

## Project Structure

```
app/
├── data/
│   ├── local/
│   │   ├── dao/              # Room DAOs
│   │   ├── database/         # Room Database
│   │   ├── entity/           # Room Entities
│   │   └── datastore/        # DataStore Preferences
│   ├── remote/
│   │   └── api/              # Speed Test Engine
│   └── repository/           # Repository Implementations
├── domain/
│   ├── model/                # Domain Models
│   ├── repository/           # Repository Interfaces
│   └── usecase/              # Use Cases
├── presentation/
│   ├── home/                 # Home Screen
│   ├── history/              # History Screen
│   ├── settings/             # Settings Screen
│   ├── server/               # Server Selection Screen
│   ├── navigation/           # Navigation
│   └── theme/                # Theme & UI Components
└── di/                       # Hilt Modules
```

## Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd android-test-speed
```

### 2. Open in Android Studio
- Open Android Studio
- Select "Open an Existing Project"
- Navigate to the cloned directory
- Click "OK"

### 3. Sync Gradle
Android Studio will automatically sync Gradle dependencies. If not, click:
`File → Sync Project with Gradle Files`

### 4. Run the App
- Connect an Android device or start an emulator
- Click the "Run" button (green triangle) or press `Shift+F10`

## Configuration

### Setting Up Test Servers

The app comes with default test servers. To add your own:

1. Modify `ServerRepositoryImpl.kt`:
```kotlin
private fun getDefaultServers(): List<Server> {
    return listOf(
        Server(
            id = "server_1",
            name = "Your Server Name",
            host = "your.server.com",
            port = 8080,
            // ... other properties
        )
    )
}
```

2. Implement server endpoints:
```
GET  /ping              - For latency tests
GET  /download?size=X   - For download tests
POST /upload            - For upload tests
```

### Building for Release

1. Create a signing config in `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("path/to/keystore.jks")
        storePassword = "your-password"
        keyAlias = "your-alias"
        keyPassword = "your-password"
    }
}
```

2. Build release APK:
```bash
./gradlew assembleRelease
```

The APK will be located at:
`app/build/outputs/apk/release/app-release.apk`

## Permissions

The app requires the following permissions:

- **INTERNET** - For network speed testing
- **ACCESS_NETWORK_STATE** - To detect network type
- **ACCESS_WIFI_STATE** - To get WiFi information
- **READ_PHONE_STATE** - To get carrier information
- **POST_NOTIFICATIONS** - For test completion notifications (Android 13+)
- **FOREGROUND_SERVICE** - To keep tests running in background

Optional:
- **ACCESS_COARSE_LOCATION** - For finding nearest server
- **ACCESS_FINE_LOCATION** - For precise server location

## Key Features Implementation

### Speed Test Algorithm

The speed test uses multi-threaded HTTP requests:

1. **Ping Test**: Sends multiple HEAD requests and calculates latency statistics
2. **Download Test**: Downloads data chunks simultaneously using multiple threads
3. **Upload Test**: Uploads random data using multiple concurrent connections

Speed calculation:
```kotlin
Speed (Mbps) = (Bytes × 8) / (Time in seconds × 1,000,000)
```

### Data Persistence

- **Test Results**: Stored in Room database with full history
- **User Preferences**: Stored in DataStore for reactive updates
- **Server List**: Cached locally with Room

### Material Design 3

The app follows Material Design 3 guidelines:
- **Dynamic Color** (Material You) on Android 12+
- **Adaptive Layouts**
- **Motion & Animations**
- **Accessibility** compliant

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write/update tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Inspired by Speedtest.net
- Built with Android Jetpack libraries
- Uses Material Design 3 components

## Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Contact: [your-email@example.com]

## Roadmap

- [ ] Chart visualization for history
- [ ] Widget for home screen
- [ ] Scheduled automatic tests
- [ ] Export data to CSV/JSON
- [ ] VPN detection
- [ ] Share test results
- [ ] Compare with previous tests
- [ ] Network quality score

---

**Built with ❤️ using Kotlin and Jetpack Compose**
