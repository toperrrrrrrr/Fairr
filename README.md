# Fairr - Android App

Fairr is a group expense management application that helps users track shared expenses and automatically calculate balances between group members. This Android application is built using modern Android development practices and Jetpack Compose.

## Features

- 👤 User Authentication
  - Email/Password login with verification
  - Biometric authentication (Fingerprint/Face ID)
  - Google Sign-in integration
  - Profile management
  - Secure session handling

- 👥 Group Management
  - Create and join groups
  - Real-time member management
  - Multiple currency support
  - Role-based permissions
  - Activity logging

- 💰 Expense Tracking
  - Add/edit/delete expenses
  - Multiple split options (Equal, Percentage, Custom)
  - Receipt management (Coming Soon)
  - Expense categorization
  - Real-time updates

- 📊 Balance Management
  - Automatic balance computation
  - Multi-currency support
  - Simplified debt resolution
  - Settlement suggestions
  - Transaction history
  - Balance visualization

- 🔐 Security Features
  - End-to-end encryption
  - Secure data storage
  - Biometric authentication
  - API key protection
  - Input validation

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **Backend**: Firebase (Authentication, Firestore, Storage)
- **Dependencies**:
  - Material3 Design Components
  - Navigation Compose
  - ViewModel Compose
  - Firebase BoM
  - Kotlin Coroutines
  - Biometric Authentication
  - Room Database (offline support)
  - WorkManager (background tasks)

## Project Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/Fairr-android.git
```

2. Open the project in Android Studio

3. Configure Firebase:
   - Create a new Firebase project
   - Add your `google-services.json` to the app directory
   - Enable Authentication and Firestore
   - Configure Storage for receipt uploads

4. Build and run the project

## Development Environment

- Android Studio Hedgehog | 2023.1.1
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Kotlin version: 1.9.22
- Compose version: 1.5.8

## Architecture

The project follows Clean Architecture principles with the following layers:

```
app/
├── data/           # Data layer
│   ├── local/     # Local storage
│   ├── remote/    # Remote data sources
│   └── repos/     # Repositories
├── domain/         # Business logic
│   ├── models/    # Domain models
│   ├── usecases/  # Use cases
│   └── repos/     # Repository interfaces
└── presentation/   # UI layer
    ├── auth/      # Authentication
    ├── groups/    # Group management
    ├── expenses/  # Expense tracking
    ├── profile/   # User profile
    └── common/    # Shared components
```

## Current Status

- Version: 0.2.0
- Status: Beta
- Next Release: Receipt Management Update
# Fairr Android App — Architecture & UI/UX Review

## Project Summary

**Fairr** is a modern Android app for group expense management. It enables users to track, split, and settle shared expenses in groups, with features like user authentication (including biometrics), real-time group and expense management, multi-currency support, and robust security. The app is built using Kotlin, Jetpack Compose (Material 3), and follows Clean Architecture with MVVM. Firebase powers authentication, storage, and database needs.

---

## Architecture Overview

- **Frontend**: Android app using Jetpack Compose for UI, Material 3 for design, and Navigation Compose for routing.
- **Backend**: Firebase (Authentication, Firestore, Storage).
- **Architecture Pattern**: Clean Architecture with MVVM.
    - **Layers**: Data (local/remote/repos), Domain (models/usecases/repos), Presentation (UI/screens/components).
- **Key Technologies**: Kotlin, Coroutines, Room, WorkManager, Biometric Auth, Firebase BoM.

---

## Key Components with Purpose

### 1. Entry Point & Navigation

- **MainActivity.kt**: Sets up the Compose content, applies the theme, and launches the app’s navigation graph.
- **FairrNavGraph (NavGraph.kt)**: Central navigation definition using Navigation Compose. Maps all major screens (Splash, Auth, Home, Groups, Expenses, Analytics, Settings, etc.) and handles parameterized navigation (e.g., groupId, expenseId).

### 2. UI Layer

- **Screens**: Organized by feature (e.g., `auth/`, `groups/`, `expenses/`, `profile/`, `settings/`).
    - [MainScreen.kt](cci:7://file:///c:/Users/Nori/Desktop/ndrd%20Prjct/Fairr/app/src/main/java/com/example/fairr/ui/screens/MainScreen.kt:0:0-0:0): Hosts the main tabbed interface with Home, Groups, Analytics, and Profile/Settings tabs. Uses a custom bottom navigation bar with a centered FAB for quick actions.
    - Feature folders (e.g., `groups/`, `expenses/`) contain screen composables for CRUD, detail, and management flows.

- **Components**:
    - [CommonComponents.kt](cci:7://file:///c:/Users/Nori/Desktop/ndrd%20Prjct/Fairr/app/src/main/java/com/example/fairr/ui/components/CommonComponents.kt:0:0-0:0): Reusable UI elements (chips, dialogs, loaders, error/empty states, snackbars, banners, animated counters, etc.).
    - [ModernUXComponents.kt](cci:7://file:///c:/Users/Nori/Desktop/ndrd%20Prjct/Fairr/app/src/main/java/com/example/fairr/ui/components/ModernUXComponents.kt:0:0-0:0): Modern, branded UI widgets (bottom navigation, FAB, loading indicators, empty/error/success states, banners, search bar, top app bar, pull-to-refresh).
    - Emphasis on modularity and previewable components for rapid UI iteration.

- **Theme**:
    - [Theme.kt](cci:7://file:///c:/Users/Nori/Desktop/ndrd%20Prjct/Fairr/app/src/main/java/com/example/fairr/ui/theme/Theme.kt:0:0-0:0), [Color.kt](cci:7://file:///c:/Users/Nori/Desktop/ndrd%20Prjct/Fairr/app/src/main/java/com/example/fairr/ui/theme/Color.kt:0:0-0:0): Custom light/dark color schemes, semantic color tokens, and typography. Theme switching is supported and accessible throughout the UI.

### 3. Data & Domain (from README and structure)

- **Data Layer**: Handles local (Room) and remote (Firebase) data sources. Repositories abstract data access.
- **Domain Layer**: Business logic, domain models, and use cases.
- **Presentation Layer**: Pure Compose UI, ViewModels (not directly seen in this pass, but implied by architecture).

---

## UI Observations

- **Design Language**: Consistent use of a modern, monochromatic design system with semantic color tokens. Material 3 components are customized for branding and accessibility.
- **Navigation**: Bottom navigation with a prominent, centered FAB for primary actions. Tabbed navigation for Home, Groups, Analytics, and Profile/Settings.
- **Componentization**: High reuse of composable components (chips, dialogs, banners, loaders, etc.) with preview annotations for easy testing.
- **State Handling**: Loading, error, and empty states are visually distinct and handled via dedicated components.
- **Responsiveness**: Layouts use `Modifier.fillMaxSize()`, padding, and arrangement for adaptive design. However, there is no explicit mention of large-screen/tablet support.
- **Accessibility**: Uses color contrast-aware tokens and descriptive icons. However, there is little direct evidence of screen reader support (content descriptions are present for icons, but not for all elements).

---

## UX Observations

- **Onboarding & Auth**: Supports multiple authentication methods, including biometrics and Google sign-in. Secure session handling is prioritized.
- **Group & Expense Flows**: Real-time group management, expense splitting (multiple methods), and activity logging. Navigation flows are clear and parameterized (e.g., passing groupId/expenseId).
- **Feedback**: Uses snackbars, banners, and animated counters for user feedback. Success, error, and loading states are visually distinct.
- **Navigation Flow**: Logical and user-friendly, with clear routes for creating/joining groups, adding/editing expenses, and accessing settings or analytics.
- **User Control**: Easy access to primary actions via FAB and bottom navigation. Settings and profile management are separated for clarity.
- **Security & Privacy**: Emphasis on encryption, secure storage, and input validation. Biometric authentication adds a layer of trust.

---

## Suggestions for Improvement

### UI Design

- **Responsiveness**: Add explicit support for tablets and foldables (adaptive layouts, larger touch targets, multi-column support).
- **Accessibility**:
    - Ensure all interactive elements have content descriptions.
    - Support dynamic font scaling and high-contrast modes.
    - Add accessibility tests and audit for screen reader compatibility.
- **Theming**: Consider user-customizable themes (color-blind modes, font choices).
- **Animations**: Use subtle motion for transitions (screen changes, FAB, banners) to enhance perceived performance and delight.
- **Visual Hierarchy**: Further differentiate primary/secondary actions (e.g., accent colors for FAB, outlined secondary buttons).

### UX Design

- **Onboarding**: Add a first-time user walkthrough to explain key features (e.g., how to split expenses, manage groups).
- **Feedback**: Expand contextual feedback—e.g., explain why an action failed (network, validation, permissions).
- **Undo/Redo**: Allow undo for destructive actions (expense/group deletion).
- **Offline Support**: Indicate offline status and sync progress clearly; allow basic actions offline with queued sync.
- **Navigation**: Add deep linking and back stack management for complex flows (e.g., notification → group detail → expense edit).
- **Personalization**: Let users customize dashboard, reorder tabs, or pin favorite groups.

---

## Concrete Next Steps

1. **Accessibility Audit**: Review all screens/components for accessibility compliance (screen reader, font scaling, color contrast).
2. **Tablet & Large Screen Support**: Implement adaptive layouts for tablets/foldables.
3. **Onboarding Walkthrough**: Add a first-launch tutorial or tips overlay.
4. **Feedback Expansion**: Add more granular error messages and success confirmations.
5. **Offline Experience**: Clarify offline/online state in the UI, and test Room/WorkManager flows.
6. **User Testing**: Conduct usability tests with real users to identify navigation pain points or confusing flows.
7. **Documentation**: Expand in-code documentation, especially for complex navigation and custom components.
8. **Feature Delivery**: Prioritize receipt management and OCR, as noted in the roadmap.
9. **Continuous Improvement**: Set up regular UI/UX reviews as features are added.

---

## Areas Lacking Clarity

- **Backend Logic**: The backend code (Firebase rules, cloud functions, etc.) is not present—ensure security and validation are robust there.
- **ViewModel & State Management**: While MVVM is mentioned, ViewModel and state handling code was not directly reviewed. Ensure ViewModel scopes and lifecycles are well-managed.
- **Testing**: No explicit evidence of UI or integration tests—add Compose UI tests and accessibility checks.
- **Analytics & Privacy**: Ensure analytics events are transparent and user privacy is respected (opt-in/out).

---

If you need further breakdowns (e.g., for a specific screen, feature, or backend integration), or want code-level suggestions for a particular area, let me know!
### Upcoming Features
- Receipt upload and OCR
- Enhanced user profiles
- Advanced group features
- Offline support improvements
- Performance optimizations

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

Please read our [Contributing Guidelines](CONTRIBUTING.md) for details.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, please:
- Check our [Documentation](docs/README.md)
- Open an [Issue](../../issues)
- Join our [Discord Community](discord-link)

## Acknowledgments

- Firebase team for the excellent backend services
- Jetpack Compose team for the modern UI toolkit
- Our contributors and early adopters 
