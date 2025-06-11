# Coding Conventions

This document outlines the coding standards, naming conventions, and best practices for the Fairr codebase. Adhering to these conventions is crucial for maintaining code quality, readability, and consistency.

## 1. Architecture

- **Clean Architecture**: The project adheres to Clean Architecture principles, separating the codebase into `data`, `domain`, and `presentation` layers.
- **MVVM (Model-View-ViewModel)**: The presentation layer uses the MVVM pattern. The `View` (Composable screen) observes the `ViewModel` for state changes and forwards user events.

## 2. State Management (UDF)

To ensure a predictable and maintainable UI, we follow a strict Unidirectional Data Flow (UDF) pattern.

- **Single State Object**: Each ViewModel must expose a single state object using `StateFlow`.
  ```kotlin
  // in ViewModel
  private val _uiState = MutableStateFlow(MyScreenUiState())
  val uiState: StateFlow<MyScreenUiState> = _uiState.asStateFlow()
  ```
- **Sealed UI State Class**: The state object should be a `data class`, and different screen states (Loading, Success, Error) should be represented by a `sealed interface`.
  ```kotlin
  sealed interface GroupDetailUiState {
      object Loading : GroupDetailUiState
      data class Success(val group: Group) : GroupDetailUiState
      data class Error(val message: String) : GroupDetailUiState
  }
  ```
- **Events for One-Off Actions**: Actions that should only be consumed once (like showing a snackbar or navigating) must be exposed via a `SharedFlow` or `Channel`.
  ```kotlin
  // in ViewModel
  private val _events = MutableSharedFlow<MyScreenEvent>()
  val events = _events.asSharedFlow()
  ```

## 3. Coroutines & Asynchronous Code

- **Dispatcher Injection**: Never hardcode `Dispatchers` (e.g., `Dispatchers.IO`) in ViewModels or Repositories. Instead, inject them via the constructor to make classes testable.
  ```kotlin
  // in Hilt module
  @Provides
  fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

  // in Repository
  class MyRepository @Inject constructor(
      private val ioDispatcher: CoroutineDispatcher
  ) { ... }
  ```
- **Use `Flow` for Data Streams**: Repositories should expose data streams using Kotlin `Flow`. This allows the UI to reactively update when the underlying data changes.

## 4. Data Layer & Firestore

- **Repository as Single Source of Truth**: The UI should only interact with repositories, never directly with data sources like Firestore.
- **Denormalization for Performance**: To avoid costly N+1 queries, denormalize data where appropriate. For example, when creating an expense, store the `paidByName` directly in the expense document instead of just the `paidById`.
- **Use Transactions for Atomic Operations**: Any operation that involves multiple writes to Firestore (e.g., adding an expense and updating the group's total) **must** be wrapped in a `Firestore Transaction` to ensure data consistency.
- **Collection Naming**: Firestore collections should be named using `lowercase_snake_case` (e.g., `user_profiles`, `group_expenses`).

## 5. Naming Conventions

- **Classes**: `PascalCase` (e.g., `GroupDetailViewModel`).
- **Functions/Variables**: `camelCase` (e.g., `calculateBalance`).
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_GROUP_MEMBERS`).
- **Composable Functions**: `PascalCase` (e.g., `GroupDetailScreen`).
- **State Holders**: Suffix with `State` or `UiState` (e.g., `GroupDetailUiState`).
- **Events**: Suffix with `Event` (e.g., `AddExpenseEvent`).

## 6. Logging

- **Consistent Tagging**: Use a consistent `TAG` for logging within a class. The `TAG` should be the name of the class.
  ```kotlin
  private const val TAG = "GroupDetailViewModel"
  ```
- **Log Levels**: Use log levels appropriately:
  - `Log.d` for development-time debugging.
  - `Log.i` for informational messages about important lifecycle events.
  - `Log.w` for warnings about potential issues.
  - `Log.e` for exceptions and critical errors, always including the exception object.
