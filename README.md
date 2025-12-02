# CPLPS - Competitive Programming Learning & Practice Suite

An Android application designed to help competitive programmers track their progress, practice problems, and improve their skills.

## Features (Planned)

### 1. Problem Browser
- View problems from different competitive programming platforms
- Filter by site, contest, rating, and topic
- Direct links to problem pages
- In-app problem viewing

### 2. User Profile & Statistics
- Interactive heatmap showing problem-solving activity
- Track total problems solved
- Topic-wise problem breakdown
- Rating graphs and progress tracking
- Problem rating distribution

### 3. Bookmark Management
- Create custom folders for organizing problems
- Categories: To Solve, Interesting Problems, Hard Problems
- Add custom tags and hints to problems
- Topic-based organization

### 4. Problem History
- View all solved problems
- Sort by date
- Track solving patterns

### 5. Learning Resources
- CP algorithms and explanations
- Code template library (topic-wise)
- Add and manage personal templates

### 6. User System
- Account creation and authentication
- User statistics tracking
- Friend list feature
- View friends' progress

## Tech Stack
- **Language**: Kotlin
- **Architecture**: MVVM
- **UI**: Material Design Components
- **Navigation**: Bottom Navigation
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Project Structure
```
CPLPS/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/cplps/android/
│   │       │   └── MainActivity.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   ├── activity_main.xml
│   │       │   │   └── item_quick_access.xml
│   │       │   ├── drawable/
│   │       │   ├── menu/
│   │       │   └── values/
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── .gitignore
```

## Getting Started

### Prerequisites
- Android Studio (latest version recommended)
- JDK 8 or higher
- Android SDK

### Installation
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device

## Development Status
🚧 **Initial Setup Complete** - Basic UI structure implemented with bottom navigation and Material Design components.

## Future Enhancements
- API integration with Codeforces and other platforms
- Database implementation with Room
- User authentication system
- Advanced statistics and visualizations
- Cloud sync for bookmarks and templates

## Contributing
This project is currently in early development. Contributions will be welcomed once the core features are implemented.

## License
TBD

## Contact
Developer: Sk Nazmus Salehin Nirob
GitHub: [@Sk-Nazmus-Salehin-Nirob](https://github.com/Sk-Nazmus-Salehin-Nirob)
