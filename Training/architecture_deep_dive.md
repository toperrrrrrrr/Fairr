# Architecture Deep Dive

This document provides a detailed look into the Clean Architecture implementation used in the Fairr application.

## Core Principle: Separation of Concerns

The architecture is designed to separate the code into independent layers, each with a specific responsibility. This separation is managed using **Hilt** for dependency injection. The core principle is the **Dependency Rule**: source code dependencies can only point inwards. This means that inner layers (like `domain`) are independent of outer layers (like `presentation` and `data`).

```
Presentation Layer -> Domain Layer <- Data Layer
```

## The Layers

### 1. Presentation Layer

- **Purpose**: This layer is responsible for everything related to the user interface. It's what the user sees and interacts with.
- **Components**: 
  - **UI (Views/Composables)**: Jetpack Compose screens that render the data.
  - **ViewModels**: Prepare and manage data for the UI. They survive configuration changes and handle user events.
- **Responsibilities**:
  - Observing data streams (e.g., from a `StateFlow`) exposed by ViewModels.
  - Displaying the data on the screen.
  - Capturing user input and forwarding it to the ViewModel.
- **Dependencies**: This layer depends on the `domain` layer. ViewModels execute `UseCases` to trigger business logic.

### 2. Domain Layer

- **Purpose**: This is the core of the application. It contains the business logic and rules that are central to the app's functionality.
- **Components**:
  - **Use Cases (or Interactors)**: Encapsulate a single, specific business rule (e.g., `AddExpense`, `CalculateBalances`).
  - **Domain Models**: Plain Kotlin data classes that represent the core entities of the application (e.g., `User`, `Group`, `Expense`).
  - **Repository Interfaces**: Define the contracts for how data should be fetched or stored. These are implemented by the `data` layer.
- **Responsibilities**:
  - Executing business logic, independent of the UI or data sources.
- **Dependencies**: This layer is completely independent. It has no dependencies on the `presentation` or `data` layers.

### 3. Data Layer

- **Purpose**: This layer is responsible for managing the application's data, whether it comes from a remote server, a local database, or a cache.
- **Components**:
  - **Repository Implementations**: Concrete implementations of the repository interfaces defined in the `domain` layer.
  - **Data Sources**: 
    - **Remote**: Handles communication with the backend (e.g., Firebase Firestore, APIs).
    - **Local**: Manages local data storage. The project uses DataStore for simple key-value data. A Room database is planned for caching to provide more robust offline support, but it is not yet implemented.
- **Responsibilities**:
  - Implementing the logic to fetch data from the appropriate source.
  - Caching data to support offline mode.
  - Mapping data models from remote/local formats to the `domain` models.
- **Dependencies**: This layer depends on the `domain` layer to implement its repository interfaces.

## Example Data Flow: Fetching Group Details

1.  **UI**: The `GroupDetailsScreen` composable calls a function on the `GroupDetailsViewModel` to fetch group information.
2.  **ViewModel**: The `GroupDetailsViewModel` executes the `GetGroupDetails` use case.
3.  **UseCase**: The `GetGroupDetails` use case in the `domain` layer calls the `getGroup()` method on the `GroupRepository` interface.
4.  **Repository**: The `GroupRepositoryImpl` in the `data` layer receives the call. It first checks the local Room database for cached data. If not found or stale, it fetches the data from Firebase Firestore.
5.  **Data Flow Back**: The data (as a `domain` model) is returned through the layers, converted into a UI state by the `ViewModel`, and finally displayed on the `GroupDetailsScreen`.
