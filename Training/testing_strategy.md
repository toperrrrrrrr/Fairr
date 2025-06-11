# Application Testing Strategy

This document outlines the comprehensive testing strategy for the Fairr application, designed to ensure correctness, reliability, and maintainability. Our approach is based on the "Testing Pyramid," emphasizing a large base of fast, isolated unit tests and progressively fewer, more integrated tests.

## Level 1: Unit Tests (`/src/test`)

**Scope**: Fast, isolated tests of individual classes or functions running on the local JVM.

- **ViewModels**: We will test all ViewModels by providing mock repositories or use cases. Key scenarios to test include:
  - The initial `UiState` is correct.
  - State is updated correctly in response to user events.
  - One-off events (e.g., navigation, errors) are emitted correctly via `SharedFlow`.
- **Business Logic**: Critical business logic, such as the **debt simplification algorithm**, will be placed in pure Kotlin classes (Use Cases) and will have extensive unit test coverage.
- **Mappers & Utilities**: All data mapping and utility functions will be unit tested to ensure they handle all expected inputs correctly.

**Tools**: `JUnit 5`, `MockK` for creating test doubles.

## Level 2: Integration Tests (`/src/androidTest`)

**Scope**: Tests that verify the interaction between different components of the app or with external systems like the database and backend services.

- **Data Layer Integration**: We will test our `Repository` implementations against the **Firebase Emulator Suite**. This allows us to verify real interactions with emulated Firestore, Authentication, and Storage services without incurring costs or relying on a network connection.
- **Room Database (DAOs)**: We will test all Data Access Objects using an in-memory Room database to ensure all SQL queries for our offline cache are correct.
- **Firebase Security Rules**: A critical part of our security posture. We will use the Firebase Emulator Suite's testing utilities to write tests that assert whether a given user can or cannot perform specific actions (read, write, delete) on our Firestore collections.
- **Firebase Functions**: The business logic within our Cloud Functions (e.g., `debtSimplification`, `sendGroupInvite`) will be unit tested using the `firebase-functions-test` library.

**Tools**: `AndroidX Test`, `Robolectric`, `Firebase Emulator Suite`.

## Level 3: UI Tests (`/src/androidTest`)

**Scope**: Tests that verify user flows and ensure the UI behaves as expected from a user's perspective. These tests are the slowest and most brittle, so they will be reserved for the most critical user journeys.

- **Hermetic UI Tests**: To keep UI tests fast and reliable, we will use **Hilt's testing APIs** to replace our real `Repository` implementations with **fake repositories**. These fakes will provide controlled, predictable data to the UI, allowing us to test all UI states (loading, success, empty, error) without needing a real backend or network connection.
- **Critical User Flows (End-to-End)**:
  - Onboarding, Login, and Registration.
  - Creating a group and successfully adding an expense.
  - Navigating to the Settle Up screen and viewing the settlement plan.

**Tools**: `Jetpack Compose Test APIs` (`createComposeRule`), `Hilt Android Testing`.

## Continuous Integration (CI)

A testing strategy is only effective if it is consistently applied. Therefore, we will set up a Continuous Integration pipeline (e.g., using GitHub Actions).

- **On Every Pull Request**: The CI server will automatically run all **Level 1 (Unit) tests**.
- **On Merge to Main**: The CI server will run all **Level 1 (Unit)** and **Level 2 (Integration)** tests.
- **Nightly/On-Demand**: The full suite, including the slower **Level 3 (UI) tests**, will be run on a nightly basis or triggered manually before a release.

This automated process ensures that no new code is merged without passing our quality checks, maintaining a high standard for the entire codebase.
