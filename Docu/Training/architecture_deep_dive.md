# Architecture Deep Dive

This document provides a detailed look into the Clean Architecture implementation used in the Fairr application, focusing on how the core principles are applied in practice.

## 1. Core Principle: The Dependency Rule

The architecture is designed to separate the code into independent layers, managed by **Hilt** for dependency injection. The core principle is the **Dependency Rule**: source code dependencies can only point inwards.

```
[Presentation Layer] --> [Domain Layer] <-- [Data Layer]
```

- The `domain` layer is completely independent.
- The `presentation` and `data` layers depend on the `domain` layer, but not on each other.

## 2. The Layers in Detail

### 2.1. Presentation Layer (UI & State Management)

- **Purpose**: To display application data on the screen and handle user interactions.
- **Key Components**:
  - **Views (Composables)**: Stateless Jetpack Compose functions that render the UI based on the state they are given.
  - **ViewModels**: Act as state producers. They execute use cases, receive data from the `domain` layer, and transform it into UI state.
- **Architectural Pattern: Unidirectional Data Flow (UDF)**
  - **State**: ViewModels expose a single, immutable `StateFlow` of a UI state data class. This represents the complete state of the screen.
  - **Events**: User actions are sent from the UI to the ViewModel as events (function calls).
  - **One-off Events**: Side effects like showing a snackbar or navigating are handled via a separate `SharedFlow` to ensure they are consumed only once.

### 2.2. Domain Layer (Business Logic)

- **Purpose**: To contain the core business logic and rules of the application.
- **Key Components**:
  - **Use Cases (Interactors)**: Represent a single business action (e.g., `AddExpense`). They orchestrate data flow to and from repository interfaces.
  - **Domain Models**: Simple Kotlin data classes representing the core entities (e.g., `Group`, `Expense`).
  - **Repository Interfaces**: Define the contracts (`fun getGroup(id: String): Flow<Group>`) that the `data` layer must implement.

### 2.3. Data Layer (Data Sources & Repositories)

- **Purpose**: To manage the application's data, abstracting away its origin (remote vs. local).
- **Key Components**:
  - **Repository Implementations**: Implement the repository interfaces from the `domain` layer. They are the single source of truth for the application's data.
  - **Data Sources**: Classes responsible for interacting with a specific data source (e.g., `FirestoreService`, `UserPreferencesDataStore`).
- **Architectural Pattern: Reactive Data Flow**
  - Repositories expose data using **Kotlin `Flow`**. This allows the `presentation` layer to subscribe to data changes and automatically update the UI in a reactive, efficient manner.

## 3. Firebase & Backend Architecture

- **Firestore as Remote Data Source**: Firestore is the primary remote data source. The `data` layer includes services that are responsible for all Firestore queries and mutations.
- **Firebase Authentication**: Used for user identity management. The `AuthRepository` abstracts away the Firebase Auth SDK.
- **Firebase Functions for Backend Logic**: For complex, sensitive, or resource-intensive operations, we will use Firebase Functions. This moves logic from the client to a secure, managed backend.
  - **Use Cases**: Debt simplification (`calculateSettlementPlan`), invite code generation, and processing receipt scans.
- **Firestore Security Rules**: Security is enforced at the backend via Firestore rules. These rules ensure that users can only access data they are authorized to see (e.g., they are a member of a group).

## 4. Offline Support Strategy (Future)

While not yet fully implemented, the architecture is designed to support a robust offline-first experience.

- **Caching with Room**: A **Room** database will be added to the `data` layer as a local data source.
- **Synchronization Strategy**: The `Repository` will be responsible for the synchronization logic:
  1.  When data is requested, the repository will first emit the cached data from Room for an instant UI update.
  2.  It will then fetch fresh data from Firestore.
  3.  Upon receiving the fresh data, it will update the Room cache, which will trigger a new emission from the `Flow`, automatically updating the UI with the latest information.

## 5. Example Data Flow: Fetching Group Details

1.  **UI**: `GroupDetailsScreen` collects the `uiState` `StateFlow` from the `GroupDetailsViewModel`.
2.  **ViewModel**: On `init`, the ViewModel launches a coroutine that executes the `GetGroupDetails` use case.
3.  **Use Case**: The use case calls `groupRepository.getGroup(groupId)`.
4.  **Repository**: `GroupRepositoryImpl` returns a `Flow`. It first queries Firestore for the group document.
5.  **Data Flow Back**: The `Flow` emits a `Group` domain model. The ViewModel catches this, maps it to a `GroupDetailsUiState.Success` object, and updates its `_uiState` `MutableStateFlow`. The UI, which is collecting the flow, automatically recomposes to display the group details.
