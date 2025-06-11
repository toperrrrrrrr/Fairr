# Project Overview

## Introduction

Fairr is a modern Android application designed for group expense management. It allows users to easily track, split, and settle shared expenses within groups. The application is built with a focus on a clean user interface, real-time data synchronization, and robust security features.

## Key Features

- **User Authentication**: Secure sign-up and login with email/password, Google Sign-in, and biometric authentication (fingerprint/face ID).
- **Group Management**: Users can create, join, and manage groups in real-time. The system supports multiple currencies and role-based permissions for group members.
- **Expense Tracking**: Add, edit, and delete expenses with various splitting options (equal, percentage, custom). Receipt management is a planned feature.
- **Balance Management**: The app automatically calculates balances between group members, provides settlement suggestions, and maintains a detailed transaction history.
- **Security**: End-to-end encryption, secure data storage, and input validation are implemented to protect user data.

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture principles
- **Backend**: Firebase (Authentication, Firestore, Storage)
- **Core Dependencies**:
  - Navigation Compose
  - ViewModel Compose
  - Kotlin Coroutines
  - Room for offline storage
  - WorkManager for background tasks
