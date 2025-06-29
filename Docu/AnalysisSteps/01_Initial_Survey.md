# Fairr Codebase Analysis - Phase 1: Initial Survey

## Project Overview

**Fairr** is a modern Android group expense management application built with Kotlin and Jetpack Compose. The app enables users to track shared expenses, manage groups, and automatically calculate balances between members.

### Key Project Details
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture principles
- **Backend**: Firebase (Authentication, Firestore, Storage)
- **Dependency Injection**: Hilt
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Version**: 1.0 (versionCode: 1)

## Project Structure

### Root Directory Layout
```
Fairr/
├── app/                    # Main Android application module
├── Docu/                   # Project documentation
├── Fairr/                  # Additional project files
├── gradle/                 # Gradle wrapper and configuration
├── build.gradle.kts        # Root project build configuration
├── settings.gradle.kts     # Project settings
├── firebase.json           # Firebase configuration
├── firestore.indexes.json  # Firestore database indexes
├── README.md               # Project documentation
├── Notes.txt               # Development notes and TODO items
└── Android logcat.text     # Debug logs
```

### Main Application Structure (`app/`)
```
app/
├── build.gradle.kts        # App-level build configuration
├── google-services.json    # Firebase configuration
├── proguard-rules.pro      # Code obfuscation rules
└── src/main/
    ├── AndroidManifest.xml # App manifest and permissions
    ├── firestore.rules     # Firestore security rules
    ├── assets/             # Static assets
    ├── java/com/example/fairr/
    │   ├── data/           # Data layer (repositories, services)
    │   ├── di/             # Dependency injection modules
    │   ├── navigation/     # Navigation configuration
    │   ├── ui/             # UI layer (screens, components, themes)
    │   ├── util/           # Utility classes
    │   ├── utils/          # Additional utilities
    │   ├── models/         # UI models
    │   ├── MainActivity.kt # Main entry point
    │   └── FairrApplication.kt # Application class
    └── res/                # Android resources
```

## Primary Entry Points

### 1. Application Entry Point
- **`FairrApplication.kt`**: Hilt-enabled Application class that initializes Firebase
- **`MainActivity.kt`**: Main activity that sets up Compose content and navigation

### 2. Navigation Structure
- **`FairrNavGraph.kt`**: Central navigation definition with 30+ screen routes
- **Navigation Pattern**: Uses Navigation Compose with parameterized routes for dynamic navigation

### 3. Key Screen Categories
- **Authentication**: Welcome, Login, SignUp, ForgotPassword, AccountVerification
- **Onboarding**: OnboardingScreen
- **Main App**: MainScreen (tabbed interface)
- **Groups**: CreateGroup, JoinGroup, GroupDetail, GroupSettings, GroupActivity
- **Expenses**: AddExpense, EditExpense, ExpenseDetail
- **Settlements**: Settlement, SettlementsOverview
- **Profile & Settings**: Settings, EditProfile, UserProfile, CurrencySelection
- **Support**: HelpSupport, PrivacyPolicy, ContactSupport

## Architectural Patterns

### 1. Clean Architecture Implementation
The project follows Clean Architecture with clear separation of concerns:

- **Data Layer** (`data/`): Handles data operations, repositories, and external services
- **Domain Layer**: Business logic and use cases (implied by structure)
- **Presentation Layer** (`ui/`): UI components, screens, and ViewModels

### 2. MVVM Pattern
- ViewModels are used for state management
- UI state is observed through Compose state collection
- Clear separation between UI logic and business logic

### 3. Dependency Injection
- **Hilt** is used for dependency injection
- Modules are organized in `di/` directory
- ViewModels are injected using `@HiltViewModel`

## Key Dependencies

### Core Android & Compose
- Jetpack Compose BOM (2024.02.00)
- Material 3 components
- Navigation Compose
- Lifecycle ViewModel Compose
- Coroutines for async operations

### Firebase Integration
- Firebase BOM (32.7.4)
- Authentication, Firestore, Storage, Analytics
- Google Sign-In integration

### UI & UX Libraries
- Accompanist (system UI controller, swipe refresh, permissions)
- Vico charts for data visualization
- Coil for image loading
- ML Kit for text recognition

### Testing
- JUnit, Mockito, Turbine for unit testing
- Compose UI testing
- Coroutines testing utilities

## Documentation & Notes

### Existing Documentation
- **`README.md`**: Comprehensive project overview with setup instructions
- **`Docu/`**: Organized documentation structure with analysis steps
- **`Notes.txt`**: Development notes with TODO items and known issues

### Key Issues Identified (from Notes.txt)
1. Session persistence issues (auto-login after sign out)
2. UI/UX improvements needed (navigation bar spacing, transitions)
3. Data flow validation required (group creation → expense recording → settlement)
4. Feature completeness review needed
5. Currency handling inconsistencies (PHP vs USD defaults)

## Development Environment

### Build Configuration
- **Kotlin Version**: 1.9.22
- **Compose Compiler**: 1.5.8
- **Java Version**: 17
- **Gradle**: Kotlin DSL

### Permissions & Features
- Camera access for receipt scanning
- Storage permissions for file handling
- Notification permissions
- Biometric authentication support

## Next Steps for Analysis

Based on this initial survey, the following phases will focus on:

1. **High-Level Architecture**: Deep dive into data flow, service interactions, and module relationships
2. **Core Features and Flows**: Analysis of user journeys and feature implementations
3. **Detailed Component Analysis**: Examination of individual modules and their responsibilities
4. **Data Models and Persistence**: Understanding data structures and storage mechanisms
5. **Testing and Quality**: Review of testing strategy and code quality
6. **Build and Deployment**: Analysis of build process and configuration

## Summary

Fairr is a well-structured Android application following modern development practices. The codebase demonstrates:
- Clear architectural separation with Clean Architecture principles
- Comprehensive feature set for group expense management
- Modern Android development stack (Compose, Hilt, Firebase)
- Organized documentation and development notes

The project shows signs of active development with identified areas for improvement, particularly around session management, UI/UX refinements, and data flow validation. 