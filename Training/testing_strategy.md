# Testing Strategy

This document outlines the testing strategy for the Fairr application, ensuring the reliability, correctness, and quality of the codebase. The strategy is divided into three main categories: Unit Tests, Integration Tests, and UI Tests.

## 1. Unit Tests

**Scope**: These tests focus on the smallest parts of the application in isolation.

- **ViewModels**: Test the logic within ViewModels by mocking the Use Cases they depend on. Verify that UI state is updated correctly in response to events and data changes.
- **Use Cases**: Test the business logic within Use Cases. Since Use Cases are pure Kotlin classes, they are straightforward to test.
- **Mappers and Utility Functions**: Test any data mapping functions or other utility classes to ensure they behave as expected.

**Tools**: JUnit, Mockito (or MockK) for mocking dependencies.

## 2. Integration Tests

**Scope**: These tests verify the interactions between different parts of the application.

- **Data Layer**: Test the `Repository` implementations. This includes testing the interaction between the local Room database and the remote Firebase Firestore data source. For example, verify that data is correctly cached locally after being fetched from the remote source.
- **Room Database**: Test the DAOs (Data Access Objects) to ensure that database queries are correct.

**Tools**: AndroidX Test libraries, Robolectric (for running tests on the JVM), and a local Room database instance.

## 3. UI Tests

**Scope**: These tests verify the UI and user flows from the user's perspective.

- **Composable Components**: Test individual Jetpack Compose components to ensure they render correctly based on different states and inputs.
- **Screen-Level Tests**: Test entire screens to verify that they display the correct data and respond to user interactions as expected.
- **End-to-End User Flows**: Test critical user journeys, such as:
  - Logging in and navigating to the home screen.
  - Creating a group and adding an expense.
  - Settling a balance.

**Tools**: Jetpack Compose testing APIs (`createComposeRule`), Espresso (for interacting with UI elements), and Hilt for providing test dependencies.

## Running Tests

Tests should be organized into the standard Android project structure (`/src/test` for unit tests and `/src/androidTest` for integration and UI tests). They can be executed from Android Studio or via the command line using Gradle.

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests (UI and integration)
./gradlew connectedAndroidTest
```
