# Fairr Development Setup Guide

This guide will help you set up your development environment for the Fairr Android application.

## Prerequisites

### Required Software
- **Android Studio** (Latest stable version, currently 2023.1.1 or newer)
- **Java Development Kit (JDK)** 17 or newer
- **Git** for version control
- **Node.js** (for Firebase CLI, if needed)

### System Requirements
- **Operating System**: Windows 10/11, macOS 10.15+, or Linux
- **RAM**: Minimum 8GB, recommended 16GB
- **Storage**: At least 10GB free space
- **Internet**: Stable connection for downloading dependencies

## Initial Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Fairr
```

### 2. Android Studio Configuration
1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the Fairr folder and select it
4. Wait for the project to sync and index

### 3. Firebase Configuration
The project uses Firebase for backend services. You'll need:

#### Firebase Project Access
- Request access to the Firebase project from the team lead
- Ensure you have the correct `google-services.json` file in the `app/` directory

#### Firebase CLI (Optional)
```bash
npm install -g firebase-tools
firebase login
firebase use <project-id>
```

### 4. Environment Variables
Create a `local.properties` file in the project root (if not present):
```properties
sdk.dir=/path/to/your/android/sdk
# Add any other local configuration
```

## Project Structure

### Key Directories
```
Fairr/
├── app/                    # Main Android application
│   ├── src/main/
│   │   ├── java/com/example/fairr/
│   │   │   ├── data/       # Data layer (repositories, services)
│   │   │   ├── di/         # Dependency injection
│   │   │   ├── ui/         # UI layer (screens, components)
│   │   │   └── navigation/ # Navigation components
│   │   └── res/            # Resources (layouts, strings, etc.)
│   └── build.gradle.kts    # App-level build configuration
├── Docu/                   # Documentation
├── build.gradle.kts        # Project-level build configuration
└── gradle/                 # Gradle wrapper
```

### Architecture Overview
- **MVVM Architecture** with Clean Architecture principles
- **Jetpack Compose** for UI
- **Hilt** for dependency injection
- **Firebase** for backend services
- **Repository Pattern** for data access

## Building the Project

### First Build
1. Sync project with Gradle files
2. Clean and rebuild:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

### Common Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Development Workflow

### 1. Branch Strategy
- Create feature branches from `main`
- Use descriptive branch names: `feature/expense-editing`, `fix/auth-persistence`
- Submit pull requests for review

### 2. Code Style
- Follow Kotlin coding conventions
- Use ktlint for code formatting
- Follow the existing naming conventions in the codebase

### 3. Testing
- Write unit tests for ViewModels and business logic
- Test UI components with Compose testing
- Ensure all new features have appropriate test coverage

### 4. Firebase Development
- Use Firebase Emulator Suite for local development
- Test Firestore rules locally before deployment
- Use Firebase Console for monitoring and debugging

## Common Issues and Solutions

### Build Issues
1. **Gradle sync fails**: Try `File > Invalidate Caches and Restart`
2. **Missing dependencies**: Check internet connection and try `./gradlew --refresh-dependencies`
3. **Firebase configuration errors**: Verify `google-services.json` is in the correct location

### Runtime Issues
1. **Authentication problems**: Check Firebase project configuration
2. **Database access denied**: Verify Firestore security rules
3. **UI not updating**: Check Compose state management and recomposition

### Performance Issues
1. **Slow builds**: Enable Gradle build cache and parallel execution
2. **Memory issues**: Increase Android Studio memory allocation
3. **Slow app performance**: Use Android Profiler to identify bottlenecks

## Development Tools

### Recommended Android Studio Plugins
- **Kotlin**: Built-in
- **Compose**: Built-in
- **Firebase**: For Firebase integration
- **Git Integration**: Built-in
- **Material Theme UI**: For better IDE appearance

### Useful Tools
- **Firebase Console**: For backend management
- **Android Device Monitor**: For debugging
- **Layout Inspector**: For UI debugging
- **Network Inspector**: For API debugging

## Getting Help

### Documentation
- Check the `Docu/` folder for comprehensive documentation
- Review `AnalysisSteps/` for detailed codebase analysis
- Read `MVP_SCOPE.md` for current project status

### Team Resources
- Ask questions in team chat
- Review existing pull requests for examples
- Pair program with experienced team members

### External Resources
- [Android Developer Documentation](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Firebase Documentation](https://firebase.google.com/docs)

## Next Steps

1. **Read the Documentation**: Start with `Docu/README.md` and `MVP_SCOPE.md`
2. **Explore the Codebase**: Familiarize yourself with the project structure
3. **Run the App**: Build and run the app on an emulator or device
4. **Pick a Simple Task**: Start with a small bug fix or feature
5. **Ask Questions**: Don't hesitate to ask for help when needed

---

*Last updated: December 2024*
*Maintained by: Development Team* 