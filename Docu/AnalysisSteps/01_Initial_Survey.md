# Phase 1: Initial Survey - Fairr Android Codebase Analysis

## Project Overview

**Fairr** is a modern Android group expense management application built with Kotlin and Jetpack Compose. The app helps users track shared expenses and automatically calculate balances between group members.

### Key Project Information
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **Backend**: Firebase (Authentication, Firestore, Storage)
- **Version**: 0.2.0 (Beta)
- **Target SDK**: 34 (Android 14)
- **Minimum SDK**: 24 (Android 7.0)

## Project Structure Overview

### Root Level Structure
```
Fairr/
├── app/                    # Main Android application module
├── Docu/                   # Documentation folder
├── build.gradle.kts        # Root build configuration
├── firebase.json           # Firebase configuration
├── firestore.indexes.json  # Firestore database indexes
├── gradle/                 # Gradle wrapper and version catalog
├── gradle.properties       # Gradle properties
├── local.properties        # Local development properties
└── README.md              # Project documentation
```

### Main Application Structure (`app/`)
```
app/
├── build.gradle.kts        # App-level build configuration
├── google-services.json    # Firebase configuration
├── proguard-rules.pro      # ProGuard rules
└── src/main/
    ├── AndroidManifest.xml # App manifest
    ├── assets/             # Static assets (SVG files)
    ├── firestore.rules     # Firestore security rules
    └── java/com/example/fairr/
        ├── data/           # Data layer
        ├── di/             # Dependency injection
        ├── FairrApplication.kt  # Application class
        ├── MainActivity.kt      # Main activity
        ├── models/         # UI models
        ├── navigation/     # Navigation components
        ├── ui/            # UI layer
        ├── util/          # Utility classes
        └── utils/         # Additional utilities
```

## Key Entry Points

### 1. Application Entry Point
- **`FairrApplication.kt`**: Hilt-enabled application class that initializes Firebase
- **`MainActivity.kt`**: Main activity that sets up Compose content and navigation

### 2. Navigation Structure
- **`FairrNavGraph.kt`**: Central navigation definition with 30+ routes
- **`Screen.kt`**: Sealed class defining all navigation routes
- **Main navigation flows**:
  - Authentication: Welcome → Login/SignUp → Main
  - Onboarding: Splash → Onboarding → Welcome
  - Main App: Main (with tabs) → Feature screens

### 3. Core Feature Areas
Based on the navigation structure, the app has these main feature areas:

#### Authentication & Onboarding
- Welcome screen
- Login/SignUp screens
- Onboarding flow
- Account verification

#### Main Application Features
- **Home**: Dashboard with recent activity
- **Groups**: Group management (create, join, detail, settings)
- **Expenses**: Expense tracking (add, edit, detail)
- **Analytics**: Data visualization and insights
- **Profile**: User profile management
- **Settings**: App configuration

#### Supporting Features
- **Friends**: Friend management
- **Settlements**: Balance settlement
- **Search**: Global search functionality
- **Notifications**: Push notification management
- **Support**: Help and support screens

## Technology Stack Analysis

### Core Dependencies
- **UI**: Jetpack Compose BOM (2024.02.00), Material 3
- **Navigation**: Navigation Compose (2.7.7)
- **Dependency Injection**: Hilt (2.50)
- **Firebase**: Firebase BOM (32.7.4) with Auth, Firestore, Analytics
- **Google Services**: Google Sign-In (20.7.0)
- **Data Storage**: DataStore Preferences (1.0.0)
- **Image Loading**: Coil (2.5.0)
- **Charts**: Vico Charts (1.13.1)
- **ML Kit**: Text recognition (16.0.0)

### Architecture Components
- **MVVM**: ViewModels with Compose integration
- **Clean Architecture**: Separated data, domain, and presentation layers
- **Repository Pattern**: Abstracted data access
- **Dependency Injection**: Hilt for service management

## Data Layer Structure

The data layer is organized by feature domains:
```
data/
├── auth/           # Authentication services
├── expenses/       # Expense management
├── friends/        # Friend management
├── groups/         # Group management
├── model/          # Data models
├── notifications/  # Notification services
├── preferences/    # User preferences
├── repository/     # Repository implementations
└── settlements/    # Settlement calculations
```

## UI Layer Structure

The UI layer follows a feature-based organization:
```
ui/
├── components/     # Reusable UI components
├── model/          # UI-specific models
├── screen/         # Screen-specific ViewModels
├── screens/        # Screen implementations (by feature)
├── theme/          # Theme and styling
└── viewmodels/     # Shared ViewModels
```

## Key Observations

### Strengths
1. **Modern Architecture**: Uses latest Android development practices
2. **Feature Organization**: Clear separation of concerns by feature
3. **Comprehensive Navigation**: Well-structured navigation with parameterized routes
4. **Rich Feature Set**: Covers all aspects of group expense management
5. **Security Focus**: Firebase security rules and authentication

### Areas for Investigation
1. **Data Models**: Need to examine the data models and their relationships
2. **Business Logic**: Understand the expense splitting and settlement algorithms
3. **State Management**: How ViewModels handle complex state
4. **Error Handling**: Error handling patterns across the app
5. **Testing**: Current testing strategy and coverage

## Next Steps

**Phase 2: High-Level Architecture** will focus on:
- Detailed examination of the architectural layers
- Data flow analysis
- Key dependencies and integrations
- Service layer organization

## Summary

Fairr is a well-structured, modern Android application with a comprehensive feature set for group expense management. The codebase follows current best practices with Jetpack Compose, Clean Architecture, and Firebase backend. The navigation structure suggests a mature application with 30+ screens covering authentication, core features, and supporting functionality.

The project appears to be in active development (Beta version 0.2.0) with a clear roadmap for future features like receipt management. 