# Fairr Codebase Analysis - Phase 2: High-Level Architecture

## Architecture Overview

Fairr follows a **Clean Architecture** pattern with **MVVM** presentation layer, implemented using modern Android development practices. The architecture is organized into distinct layers with clear separation of concerns.

### Architectural Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │   Screens   │ │ ViewModels  │ │ Components  │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │   Models    │ │ Use Cases   │ │ Interfaces  │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │ Repositories│ │  Services   │ │   Models    │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                   External Dependencies                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │   Firebase  │ │   Hilt DI   │ │   Android   │          │
│  │  (Auth/DB)  │ │             │ │   System    │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

## Core Modules and Services

### 1. Dependency Injection (Hilt)

#### AppModule.kt
- **FirebaseFirestore**: Configured with offline persistence and unlimited cache
- **GroupService**: Group management operations
- **ActivityService**: Activity tracking and logging
- **ExpenseRepository**: Core expense data operations
- **GroupJoinService**: Group joining functionality
- **NotificationService**: Push notification management
- **GroupInviteService**: Group invitation handling
- **SettlementService**: Balance calculation and settlement
- **RecurringExpenseNotificationService**: Background notification scheduling

#### AuthModule.kt
- **FirebaseAuth**: Authentication provider
- **AuthService**: Authentication operations
- **UserRepository**: User data management
- **GoogleAuthService**: Google Sign-In integration

### 2. Data Layer Architecture

#### Repository Pattern
The app uses a repository pattern to abstract data access:

```kotlin
interface ExpenseRepository {
    suspend fun addExpense(...)
    suspend fun getExpensesByGroupId(groupId: String): List<Expense>
    fun getExpensesByGroupIdFlow(groupId: String): Flow<List<Expense>>
    suspend fun updateExpense(oldExpense: Expense, newExpense: Expense)
    suspend fun deleteExpense(expense: Expense)
    // ... recurring expense methods
}
```

#### Service Layer
Services handle business logic and external integrations:

- **AuthService**: Authentication state management, session validation
- **GroupService**: Group CRUD operations, real-time updates
- **ExpenseRepository**: Expense management with complex splitting logic
- **SettlementService**: Balance calculation algorithms
- **NotificationService**: Push notification handling

### 3. Data Models

#### Core Entities

**Expense Model**
```kotlin
data class Expense(
    val id: String = "",
    val groupId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val currency: String = "PHP",
    val date: Timestamp = Timestamp.now(),
    val paidBy: String = "", // userId
    val paidByName: String = "", // user's name
    val splitBetween: List<ExpenseSplit> = emptyList(),
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val notes: String = "",
    val attachments: List<String> = emptyList(),
    val splitType: String = "Equal Split",
    val isRecurring: Boolean = false,
    val recurrenceRule: RecurrenceRule? = null,
    val parentExpenseId: String? = null
)
```

**Group Model**
```kotlin
data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = "PHP",
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",
    val inviteCode: String = "",
    val members: List<GroupMember> = emptyList(),
    val avatar: String = "",
    val avatarType: AvatarType = AvatarType.EMOJI
)
```

**ExpenseSplit Model**
```kotlin
data class ExpenseSplit(
    val userId: String,
    val userName: String,
    val share: Double,
    val isPaid: Boolean = false
)
```

### 4. Presentation Layer Architecture

#### Navigation Structure
The app uses **Navigation Compose** with a centralized navigation graph:

- **30+ Screen Routes**: Organized by feature (auth, groups, expenses, etc.)
- **Parameterized Navigation**: Dynamic routes for group/expense details
- **Tabbed Interface**: Main screen with 5 tabs (Home, Groups, Friends, Notifications, Settings)

#### ViewModel Pattern
ViewModels manage UI state and business logic:

- **StartupViewModel**: App initialization and authentication state
- **Feature-specific ViewModels**: Each major feature has its own ViewModel
- **State Management**: Uses StateFlow for reactive state updates

#### UI Components
- **Reusable Components**: Common UI elements in `components/` package
- **Theme System**: Material 3 theming with custom color schemes
- **Responsive Design**: Adaptive layouts using Compose modifiers

## Data Flow Architecture

### 1. Authentication Flow

```
App Startup → StartupViewModel → AuthService → Firebase Auth
     ↓
Session Validation → UserPreferencesManager → Navigation Decision
     ↓
Main App / Authentication Screens
```

### 2. Group Management Flow

```
UI Action → ViewModel → GroupService → Firestore
     ↓
Real-time Updates → Flow → UI State Update
     ↓
Activity Logging → ActivityService
```

### 3. Expense Management Flow

```
Add Expense → ViewModel → ExpenseRepository → Firestore
     ↓
Split Calculation → AdvancedSplitCalculator
     ↓
Balance Update → SettlementService
     ↓
Activity Logging → ActivityService
     ↓
Notification → NotificationService
```

### 4. Real-time Data Synchronization

The app uses **Firebase Firestore** with real-time listeners:

```kotlin
// Example: Real-time group updates
fun getUserGroups(): Flow<List<Group>> = callbackFlow {
    val subscription = groupsCollection
        .whereArrayContains("memberIds", currentUser.uid)
        .addSnapshotListener { snapshot, error ->
            // Process updates and emit new state
        }
    awaitClose { subscription.remove() }
}
```

## Key Dependencies and Integrations

### 1. Firebase Integration

**Authentication**
- Firebase Auth for user authentication
- Google Sign-In integration
- Session management and token refresh

**Database**
- Firestore for real-time data storage
- Offline persistence enabled
- Security rules for data access control

**Storage**
- Firebase Storage for file uploads (receipts, avatars)
- File provider configuration for camera access

### 2. External Libraries

**UI & Navigation**
- Jetpack Compose BOM (2024.02.00)
- Navigation Compose (2.7.7)
- Material 3 components
- Accompanist libraries (system UI, swipe refresh, permissions)

**Data & State**
- Kotlin Coroutines for async operations
- StateFlow for reactive state management
- DataStore for preferences

**Utilities**
- Coil for image loading
- Vico charts for data visualization
- ML Kit for text recognition (receipt scanning)

### 3. Testing Dependencies

**Unit Testing**
- JUnit, Mockito, Turbine
- Coroutines testing utilities
- Firebase testing support

**UI Testing**
- Compose UI testing
- Espresso for integration tests

## Security Architecture

### 1. Authentication Security
- Firebase Auth with email/password and Google Sign-In
- Session validation and token refresh
- Biometric authentication support

### 2. Data Security
- Firestore security rules for data access control
- User-based permissions and group membership validation
- Input validation and sanitization

### 3. Network Security
- HTTPS communication with Firebase
- API key protection
- Secure file uploads

## Performance Considerations

### 1. Data Optimization
- Firestore offline persistence for offline support
- Efficient queries with proper indexing
- Pagination for large datasets

### 2. UI Performance
- Compose recomposition optimization
- Lazy loading for lists and images
- Background processing for heavy operations

### 3. Memory Management
- Proper lifecycle management in ViewModels
- Resource cleanup in coroutines
- Image caching with Coil

## Error Handling Strategy

### 1. Network Errors
- Retry mechanisms for failed requests
- Offline state handling
- User-friendly error messages

### 2. Data Validation
- Input validation at multiple layers
- Graceful handling of malformed data
- Fallback values for missing data

### 3. Authentication Errors
- Session expiration handling
- Automatic token refresh
- Clear error messages for auth failures

## Areas for Investigation

### 1. Data Consistency
- How the app handles concurrent updates
- Conflict resolution strategies
- Data synchronization across devices

### 2. Scalability
- Performance with large groups/expense lists
- Database query optimization
- Caching strategies

### 3. Testing Coverage
- Unit test coverage for business logic
- Integration test coverage
- UI test automation

## Summary

Fairr demonstrates a well-architected Android application with:

- **Clean Architecture** with clear layer separation
- **Modern Android Development** practices (Compose, Hilt, Coroutines)
- **Real-time Data Synchronization** with Firebase
- **Comprehensive Feature Set** for group expense management
- **Security-First Approach** with proper authentication and data protection

The architecture supports scalability and maintainability while providing a smooth user experience with real-time updates and offline capabilities.

## Next Steps

**Phase 3: Core Features and Flows** will focus on:
- Detailed analysis of user journeys
- Feature implementation patterns
- Business logic complexity
- User experience flows 