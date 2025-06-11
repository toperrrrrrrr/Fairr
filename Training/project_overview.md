# Project Overview

## 1. Introduction

Fairr is a modern Android application designed for transparent and efficient group expense management. It allows users to easily track, split, and settle shared expenses within groups. The application is built with a focus on a clean user interface, a reactive architecture, and a clear path toward robust offline capabilities and security.

## 2. Key Features

The feature set is broken down by its current implementation status:

### Implemented
- **User Authentication**: Secure sign-up and login with email/password and Google Sign-in.
- **Group Management**: Users can create, join, and manage groups.
- **Basic Expense Tracking**: Add and view expenses with an equal split.

### Partially Implemented / In-Progress
- **Balance Management**: The UI for displaying balances exists, but the core calculation logic is a **top-priority `TODO`**.
- **Settlement Flow**: A UI exists for settling up, but it uses sample data. The backend logic for calculating settlement plans and recording payments is not yet implemented.
- **Advanced Expense Splitting**: The data models support advanced splits (by percentage, exact amount), but the UI for this is not yet built.

### Planned
- **Full Offline Support**: Caching data with Room to allow most app functions to work offline.
- **Receipt Management**: Uploading, attaching, and viewing receipt images using Firebase Storage.
- **Biometric Authentication**: Adding fingerprint/face ID for quick and secure login.
- **User-Managed Categories**: Allowing users to create and manage their own expense categories.
- **Robust Search & Filtering**: Implementing powerful search and filtering capabilities.

## 3. Technology Stack & Architecture

Fairr is built on a modern, reactive Android technology stack.

- **Language**: **Kotlin** (100%)
- **UI Framework**: **Jetpack Compose** with **Material 3**.
- **Core Architecture**: **MVVM** combined with **Clean Architecture** principles.
- **State Management**: **Unidirectional Data Flow (UDF)** using Kotlin's `StateFlow` and `SharedFlow`.
- **Dependency Injection**: **Hilt** is used throughout the app to manage dependencies.
- **Asynchronicity**: **Kotlin Coroutines** and **Flow** are used for all asynchronous operations and to create a reactive data layer.
- **Backend**:
  - **Firebase Authentication**: For user identity.
  - **Firebase Firestore**: As the primary remote database.
  - **Firebase Storage**: Planned for receipt image hosting.
  - **Firebase Functions**: Planned for executing secure, server-side logic (e.g., debt simplification, balance calculations).
- **Local Storage**:
  - **Jetpack DataStore**: Used for simple, key-value storage (e.g., user preferences).
  - **Room**: Planned for implementing a robust offline cache.

## 4. Architectural Goals

The project's architecture is designed to achieve the following key goals:

- **Separation of Concerns**: By strictly following Clean Architecture, we keep UI, business logic, and data access code independent and maintainable.
- **Testability**: Injecting all dependencies (including `Dispatchers`) makes each layer of the application independently testable.
- **Reactivity**: Using `Flow` from the data layer up to the UI ensures the app is efficient and automatically reacts to data changes.
- **Scalability & Performance**: By planning to offload complex logic to Firebase Functions and denormalizing Firestore data, the app is designed to remain fast and cost-effective as it scales.
- **Security**: By planning for comprehensive Firestore Security Rules, we ensure that data access is securely controlled on the backend.
