# Phase 2: High-Level Architecture - Fairr Android Codebase Analysis

## Architecture Overview

Fairr follows a **Clean Architecture** pattern with **MVVM** presentation layer, organized into distinct layers with clear separation of concerns.

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
│  │ Repositories│ │   Services  │ │   Models    │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                   Infrastructure                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │   Firebase  │ │   Hilt DI   │ │   DataStore │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

## Data Layer Analysis

### Core Data Models

#### 1. Expense Model
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
    val attachments: List<String> = emptyList() // URLs to attachments
)
```

**Key Features:**
- Flexible splitting with `ExpenseSplit` objects
- Category-based organization
- Attachment support for receipts
- Multi-currency support

#### 2. Group Model
```kotlin
data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = "PHP",
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",
    val inviteCode: String = "",
    val members: List<GroupMember> = emptyList()
)
```

**Key Features:**
- Role-based member management (ADMIN/MEMBER)
- Invite code system for group joining
- Currency specification per group

#### 3. Group Management Models
- `GroupJoinRequest`: Handles join requests with approval workflow
- `GroupInvite`: Manages direct invitations
- `Notification`: System-wide notification model

### Repository Pattern Implementation

#### ExpenseRepository
**Responsibilities:**
- CRUD operations for expenses
- Complex expense splitting calculations
- Group total updates
- Transaction management

**Key Methods:**
```kotlin
interface ExpenseRepository {
    suspend fun addExpense(groupId: String, description: String, amount: Double, date: Date, paidBy: String, splitType: String)
    suspend fun getExpensesByGroupId(groupId: String): List<Expense>
    suspend fun updateExpense(oldExpense: Expense, newExpense: Expense)
    suspend fun deleteExpense(expense: Expense)
}
```

**Splitting Algorithms:**
1. **Equal Split**: Divides amount equally among all members
2. **Percentage Split**: Uses custom percentages for each member
3. **Custom Amount**: Allows specific amounts per member

#### UserRepository
- Manages user profile data
- Handles user preferences and settings
- Integrates with Firebase Auth

### Service Layer

#### GroupService
**Responsibilities:**
- Group CRUD operations
- Real-time group updates using Firestore listeners
- Member management
- Invite code generation

**Key Features:**
- Real-time data synchronization
- Member role management
- Group validation and permissions

#### SettlementService
**Core Business Logic:**
```kotlin
suspend fun calculateGroupSettlements(groupId: String): List<DebtInfo>
suspend fun getSettlementSummary(groupId: String): List<SettlementSummary>
suspend fun recordSettlement(groupId: String, payerId: String, payeeId: String, amount: Double)
```

**Settlement Algorithm:**
1. **Balance Calculation**: Tracks total paid vs. total owed per user
2. **Debt Optimization**: Minimizes number of transactions needed
3. **Settlement Recording**: Updates expense splits and creates settlement records

#### Authentication Services
- `AuthService`: Email/password authentication
- `GoogleAuthService`: Google Sign-In integration
- Session management and validation

## Presentation Layer Analysis

### ViewModel Architecture

#### StartupViewModel
**State Management:**
```kotlin
sealed class StartupState {
    object Loading : StartupState()
    object Onboarding : StartupState()
    object Authentication : StartupState()
    object Main : StartupState()
}
```

**Key Responsibilities:**
- Session validation on app startup
- Authentication state management
- Onboarding flow control
- Error handling and recovery

#### Feature-Specific ViewModels
- `GroupListViewModel`: Manages group listing and filtering
- `AddExpenseViewModel`: Handles expense creation workflow
- `SettlementViewModel`: Manages settlement calculations and UI state

### UI Component Architecture

#### Screen Organization
```
ui/screens/
├── auth/           # Authentication flows
├── groups/         # Group management
├── expenses/       # Expense tracking
├── settlements/    # Balance settlement
├── profile/        # User profile
├── settings/       # App configuration
└── common/         # Shared screens
```

#### Component Library
- `CommonComponents`: Reusable UI elements (chips, dialogs, loaders)
- `ModernUXComponents`: Branded UI widgets (navigation, FAB, banners)
- `ImageComponents`: Profile images and media handling

## Dependency Injection (Hilt)

### Module Structure

#### AppModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun provideFirebaseFirestore(): FirebaseFirestore
    @Provides @Singleton fun provideGroupService(auth: FirebaseAuth, firestore: FirebaseFirestore): GroupService
    @Provides @Singleton fun provideExpenseRepository(firestore: FirebaseFirestore, auth: FirebaseAuth): ExpenseRepository
    // ... other services
}
```

#### AuthModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides @Singleton fun provideFirebaseAuth(): FirebaseAuth
    @Provides @Singleton fun provideAuthService(auth: FirebaseAuth): AuthService
    @Provides @Singleton fun provideGoogleAuthService(auth: FirebaseAuth, context: Context, userRepository: UserRepository): GoogleAuthService
}
```

### Service Dependencies
- **Firebase Services**: Auth, Firestore, Storage
- **Repository Dependencies**: Services depend on repositories for data access
- **Cross-Service Dependencies**: SettlementService depends on ExpenseRepository

## Data Flow Analysis

### Authentication Flow
```
1. App Startup → StartupViewModel.validateSessionOnStartup()
2. Check Onboarding → UserPreferencesManager.onboardingCompleted
3. Validate Session → AuthService.validateCurrentSession()
4. Firebase Auth → Real-time auth state monitoring
5. Navigation → FairrNavGraph.handleAuthRedirect()
```

### Expense Creation Flow
```
1. UI Input → AddExpenseScreen
2. Validation → AddExpenseViewModel
3. Business Logic → ExpenseRepository.addExpense()
4. Data Persistence → Firebase Firestore
5. Group Update → Group total calculation
6. UI Update → Real-time listener updates
```

### Settlement Calculation Flow
```
1. Group Selection → SettlementScreen
2. Data Fetching → SettlementService.calculateGroupSettlements()
3. Balance Calculation → Expense aggregation per user
4. Debt Optimization → Greedy algorithm for minimal transactions
5. UI Display → Settlement summary and debt relationships
```

## Key Architectural Patterns

### 1. Repository Pattern
- **Abstraction**: Interfaces define data access contracts
- **Implementation**: Concrete classes handle Firebase operations
- **Benefits**: Testability, flexibility, separation of concerns

### 2. Service Layer Pattern
- **Business Logic**: Complex operations encapsulated in services
- **Coordination**: Services orchestrate multiple repositories
- **Reusability**: Services can be used across different ViewModels

### 3. MVVM with StateFlow
- **State Management**: StateFlow for reactive UI updates
- **Lifecycle Awareness**: ViewModels handle configuration changes
- **Unidirectional Data Flow**: UI → ViewModel → Repository → Service

### 4. Dependency Injection
- **Loose Coupling**: Services and repositories injected via Hilt
- **Testability**: Easy to mock dependencies for testing
- **Singleton Management**: Proper lifecycle management of services

## Data Persistence Strategy

### Firebase Firestore
- **Real-time Updates**: Snapshot listeners for live data
- **Offline Support**: Local caching with Firestore settings
- **Security Rules**: Document-level access control
- **Collections Structure**:
  - `users`: User profiles and preferences
  - `groups`: Group data and member management
  - `expenses`: Expense records with splits
  - `settlements`: Settlement transaction records
  - `notifications`: System notifications

### DataStore Preferences
- **User Preferences**: Onboarding status, theme settings
- **Session Management**: Authentication state persistence
- **Local Configuration**: App settings and user preferences

## Error Handling Strategy

### Repository Level
- **Try-Catch Blocks**: Comprehensive exception handling
- **Logging**: Detailed error logging for debugging
- **Graceful Degradation**: Fallback to default values when possible

### Service Level
- **Result Types**: Success/Error result patterns
- **Validation**: Input validation before processing
- **Transaction Rollback**: Firestore transaction error handling

### Presentation Level
- **State Management**: Error states in ViewModels
- **User Feedback**: Error messages and retry mechanisms
- **Loading States**: Proper loading indicators during operations

## Performance Considerations

### 1. Real-time Data
- **Efficient Queries**: Indexed Firestore queries
- **Snapshot Listeners**: Real-time updates with proper cleanup
- **Pagination**: Lazy loading for large datasets

### 2. Memory Management
- **ViewModel Scoping**: Proper lifecycle management
- **Flow Collection**: Automatic cleanup of coroutines
- **Image Loading**: Coil for efficient image caching

### 3. Network Optimization
- **Offline Support**: Firestore offline persistence
- **Batch Operations**: Grouped Firestore operations
- **Caching Strategy**: Local data caching

## Security Architecture

### Firebase Security Rules
- **Document-level Access**: Users can only access their data
- **Group Membership**: Validation of group membership
- **Role-based Permissions**: Admin vs. member permissions

### Authentication
- **Multi-provider**: Email/password + Google Sign-In
- **Session Management**: Secure session persistence
- **Biometric Support**: Additional security layer

## Summary

The Fairr codebase demonstrates a well-architected Android application with:

**Strengths:**
1. **Clean Architecture**: Clear separation of concerns
2. **Modern Patterns**: MVVM, Repository, Service Layer
3. **Real-time Data**: Efficient Firebase integration
4. **Complex Business Logic**: Sophisticated settlement algorithms
5. **Scalable Structure**: Feature-based organization

**Areas for Investigation:**
1. **Testing Strategy**: Unit and integration test coverage
2. **Performance Optimization**: Query optimization and caching
3. **Error Recovery**: Offline handling and retry mechanisms
4. **Security Hardening**: Additional security measures

## Next Steps

**Phase 3: Core Features and Flows** will focus on:
- Detailed analysis of expense splitting algorithms
- Group management workflows
- Settlement calculation complexity
- User interaction patterns
- Feature integration points 