# AI TRAINING DATA: Firebase Schema & Data Models - Fairr Android App

## 1. FIRESTORE DATABASE STRUCTURE

### **Collection Architecture Overview**
```
Firestore Database Root
├── users/{userId} → User profiles and preferences
├── groups/{groupId} → Group information and member lists
├── expenses/{expenseId} → Individual expense records
├── groupActivities/{activityId} → Group activity timeline
├── friendRequests/{requestId} → Friend relationship requests
├── userReports/{reportId} → User moderation reports
├── securityAudit/{auditId} → Security event logs
└── recurringExpenses/{ruleId} → Recurring expense rules
→ Pattern: Flat collection structure optimized for queries
```

### **Data Model Relationships**
```
USER (1) ←→ (M) GROUP_MEMBERSHIP ←→ (M) GROUP
USER (1) ←→ (M) EXPENSE (as paidBy)
GROUP (1) ←→ (M) EXPENSE
GROUP (1) ←→ (M) GROUP_ACTIVITY
USER (1) ←→ (M) FRIEND_REQUEST (as requester/target)
→ Pattern: Denormalized data for query performance
```

## 2. DETAILED COLLECTION SCHEMAS

### **Users Collection (`/users/{userId}`)**
```typescript
// SCHEMA: User profile with privacy controls
interface UserDocument {
  // Identity fields
  uid: string;                    // Firebase Auth UID (matches document ID)
  email: string;                  // User's email address
  displayName: string;            // Public display name
  photoURL?: string;              // Profile picture URL
  
  // Profile information
  firstName?: string;             // Given name
  lastName?: string;              // Family name
  phoneNumber?: string;           // Phone number (optional)
  dateOfBirth?: Timestamp;        // Birth date for age verification
  
  // App preferences
  defaultCurrency: string;        // ISO currency code (default: "USD")
  language: string;               // Language preference (default: "en")
  timezone: string;               // User's timezone
  
  // Privacy settings
  isPublic: boolean;              // Profile visibility
  allowFriendRequests: boolean;   // Accept friend requests
  shareAnalytics: boolean;        // Analytics consent
  
  // Account status
  isVerified: boolean;            // Email verification status
  isActive: boolean;              // Account status
  createdAt: Timestamp;           // Account creation time
  lastLoginAt: Timestamp;         // Last login timestamp
  
  // Moderation fields
  reportCount: number;            // Number of reports against user
  isBlocked: boolean;             // Moderation status
  
  // Analytics fields (anonymized)
  totalExpenses: number;          // Count of expenses created
  totalGroups: number;            // Count of groups joined
  lastActivityAt: Timestamp;      // Last app activity
}

// EXAMPLE: User document from data/model/User.kt
data class User(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoURL: String? = null,
    val defaultCurrency: String = "USD",
    val isPublic: boolean = true,
    val createdAt: Timestamp = Timestamp.now()
)
```

**AI Learning Points:**
- Comprehensive user profile with privacy controls
- Denormalized data for quick access (totalExpenses, totalGroups)
- Moderation fields for user safety
- Timezone and localization support

### **Groups Collection (`/groups/{groupId}`)**
```typescript
// SCHEMA: Group with member management and settings
interface GroupDocument {
  // Basic information
  id: string;                     // Auto-generated group ID
  name: string;                   // Group display name
  description?: string;           // Optional group description
  category: string;               // Group category (trip, household, etc.)
  
  // Group settings
  currency: string;               // Group's default currency
  isPrivate: boolean;             // Private/public group status
  allowJoinRequests: boolean;     // Allow join requests from non-members
  
  // Member management
  members: {                      // Map of userId -> member info
    [userId: string]: {
      name: string;               // Member's display name (cached)
      email: string;              // Member's email (cached)
      role: "ADMIN" | "MEMBER";   // Member role
      joinedAt: Timestamp;        // When member joined
      invitedBy?: string;         // Who invited this member
      permissions: string[];      // Custom permissions
    }
  };
  memberIds: string[];            // Array for query optimization
  memberCount: number;            // Cached member count
  
  // Group lifecycle
  createdBy: string;              // Creator's user ID
  createdAt: Timestamp;           // Creation timestamp
  lastActivity: Timestamp;        // Last activity in group
  
  // Financial tracking
  totalExpenses: number;          // Sum of all expenses
  expenseCount: number;           // Count of expenses
  lastExpenseAt?: Timestamp;      // Most recent expense
  
  // Group features
  features: {
    allowRecurringExpenses: boolean;
    enableSettlements: boolean;
    requireApproval: boolean;
    enableComments: boolean;
  };
  
  // Analytics (anonymized)
  activityLevel: "LOW" | "MEDIUM" | "HIGH";
}

// EXAMPLE: Group model from data/model/Group.kt
data class Group(
    val id: String,
    val name: String,
    val description: String = "",
    val currency: String = "USD",
    val members: List<GroupMember>,
    val createdBy: String,
    val createdAt: Timestamp,
    val totalExpenses: Double = 0.0
)
```

**AI Learning Points:**
- Member management with roles and permissions
- Cached aggregated data for performance (memberCount, totalExpenses)
- Feature flags for group functionality
- Activity tracking for engagement analytics

### **Expenses Collection (`/expenses/{expenseId}`)**
```typescript
// SCHEMA: Expense with split calculation and audit trail
interface ExpenseDocument {
  // Basic expense information
  id: string;                     // Auto-generated expense ID
  groupId: string;                // Reference to parent group
  description: string;            // Expense description
  amount: number;                 // Total expense amount
  currency: string;               // Currency code
  
  // Payment information
  paidBy: string;                 // User ID who paid
  paidByName: string;             // Cached payer name
  paymentMethod?: string;         // Payment method (cash, card, etc.)
  
  // Date information
  date: Timestamp;                // When expense occurred
  createdAt: Timestamp;           // When record was created
  updatedAt?: Timestamp;          // Last modification time
  
  // Split calculation
  splitType: "Equal" | "Percentage" | "Custom" | "Unequal";
  splitBetween: {                 // Array of split details
    userId: string;               // Member user ID
    userName: string;             // Cached member name
    share: number;                // Member's share amount
    percentage?: number;          // Share percentage (for percentage splits)
    isPaid: boolean;              // Whether member has been settled
  }[];
  
  // Categorization
  category: string;               // Expense category
  tags: string[];                 // Custom tags
  
  // Additional data
  notes?: string;                 // Optional notes
  attachments: string[];          // Image/receipt URLs
  location?: {                    // Location data
    name: string;
    latitude: number;
    longitude: number;
  };
  
  // Recurring expense data
  isRecurring: boolean;           // Part of recurring series
  recurrenceId?: string;          // Link to recurring rule
  recurringInstanceId?: string;   // Instance ID in series
  
  // Audit trail
  createdBy: string;              // Who created the expense
  modifiedBy?: string;            // Who last modified
  version: number;                // Version for optimistic locking
  
  // Settlement tracking
  isSettled: boolean;             // Whether expense is settled
  settledAt?: Timestamp;          // Settlement completion time
  settlementIds: string[];        // Related settlement records
}

// EXAMPLE: Expense model from data/model/Expense.kt
data class Expense(
    val id: String,
    val groupId: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val date: Timestamp,
    val paidBy: String,
    val paidByName: String,
    val splitBetween: List<ExpenseSplit>,
    val category: ExpenseCategory,
    val splitType: String = "Equal Split"
)
```

**AI Learning Points:**
- Complex split calculation data structure
- Audit trail with versioning for financial records
- Location and attachment support for rich expense data
- Settlement tracking for debt resolution

### **Group Activities Collection (`/groupActivities/{activityId}`)**
```typescript
// SCHEMA: Activity timeline for group engagement
interface GroupActivityDocument {
  // Activity identification
  id: string;                     // Auto-generated activity ID
  groupId: string;                // Reference to group
  type: "EXPENSE_ADDED" | "MEMBER_JOINED" | "EXPENSE_SETTLED" | "GROUP_CREATED";
  
  // Activity details
  title: string;                  // Human-readable title
  description: string;            // Detailed description
  timestamp: Timestamp;           // When activity occurred
  
  // Actor information
  userId?: string;                // User who performed action
  userName?: string;              // Cached user name
  
  // Related entities
  expenseId?: string;             // Related expense (if applicable)
  amount?: number;                // Amount involved (for financial activities)
  currency?: string;              // Currency for amount
  
  // Activity metadata
  metadata: {                     // Type-specific additional data
    [key: string]: any;
  };
  
  // Visibility and notifications
  isVisible: boolean;             // Show in timeline
  notificationSent: boolean;      // Whether notifications were sent
  
  // Analytics
  engagementScore: number;        // Activity importance score
}

// EXAMPLE: Activity generation from ui/screens/groups/GroupDetailViewModel.kt
private fun generateActivities(group: Group, expenses: List<Expense>): List<GroupActivity> {
    val activities = mutableListOf<GroupActivity>()
    
    // Add group creation activity
    activities.add(
        GroupActivity(
            id = "group_created_${group.id}",
            type = ActivityType.GROUP_CREATED,
            title = "Group Created",
            description = "${group.name} was created",
            timestamp = group.createdAt,
            userId = group.createdBy
        )
    )
    
    // Add expense activities
    expenses.forEach { expense ->
        activities.add(
            GroupActivity(
                id = "expense_added_${expense.id}",
                type = ActivityType.EXPENSE_ADDED,
                title = "Expense Added",
                description = "${expense.description} - ${expense.paidByName} paid ${expense.amount}",
                timestamp = expense.date,
                userId = expense.paidBy,
                expenseId = expense.id,
                amount = expense.amount
            )
        )
    }
    
    return activities.sortedByDescending { it.timestamp.seconds }.take(20)
}
```

**AI Learning Points:**
- Activity timeline for user engagement
- Type-specific metadata for flexible activity data
- Notification tracking to prevent spam
- Engagement scoring for timeline importance

## 3. DATA RELATIONSHIP PATTERNS

### **Query Optimization Patterns**
```typescript
// PATTERN: Denormalized data for efficient queries
// Instead of joining collections, cache frequently accessed data

// Group members cached in group document
members: {
  "user123": {
    name: "John Doe",        // Cached from users collection
    email: "john@example.com", // Cached from users collection
    role: "ADMIN"
  }
}

// User names cached in expense splits
splitBetween: [
  {
    userId: "user123",
    userName: "John Doe",    // Cached to avoid user lookups
    share: 25.50
  }
]

// Aggregated data cached for performance
totalExpenses: 1250.75,      // Sum calculated and cached
expenseCount: 15,            // Count cached for quick access
memberCount: 4               // Member count cached
```

**AI Learning Points:**
- Denormalization strategy for NoSQL performance
- Strategic data caching to reduce query complexity
- Aggregate calculations stored for quick access
- Trade-off between storage space and query performance

### **Security Rule Patterns**
```javascript
// PATTERN: Security rules enforcing data model integrity
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // User can only access their own profile
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Group access based on membership
    match /groups/{groupId} {
      allow read: if request.auth != null && 
        request.auth.uid in resource.data.memberIds;
      
      allow write: if request.auth != null && 
        request.auth.uid == resource.data.createdBy;
    }
    
    // Expense access through group membership
    match /expenses/{expenseId} {
      allow read, write: if request.auth != null && 
        exists(/databases/$(database)/documents/groups/$(resource.data.groupId)) &&
        request.auth.uid in get(/databases/$(database)/documents/groups/$(resource.data.groupId)).data.memberIds;
    }
  }
}
```

**AI Learning Points:**
- Security rules enforcing business logic
- Cross-document authorization checks
- Resource-based access control
- Data model security at database level

## 4. DATA MIGRATION & VERSIONING PATTERNS

### **Schema Evolution Strategy**
```typescript
// PATTERN: Versioned data models for safe migrations
interface VersionedDocument {
  // Schema version for migration tracking
  schemaVersion: number;          // Current: 3
  
  // Migration metadata
  migratedAt?: Timestamp;         // When last migrated
  migratedFrom?: number;          // Previous schema version
  
  // Document data (version-specific)
  ...documentFields
}

// MIGRATION: User schema v2 → v3 (adding privacy settings)
// Old v2 schema:
interface UserDocumentV2 {
  uid: string;
  email: string;
  displayName: string;
  createdAt: Timestamp;
}

// New v3 schema adds privacy controls:
interface UserDocumentV3 extends UserDocumentV2 {
  schemaVersion: 3;
  privacySettings: {
    isPublic: boolean;
    allowFriendRequests: boolean;
    shareAnalytics: boolean;
  };
}

// Migration logic in app startup
async function migrateUserDocument(userId: string) {
  const userDoc = await firestore.collection('users').doc(userId).get();
  const userData = userDoc.data();
  
  if (!userData.schemaVersion || userData.schemaVersion < 3) {
    // Apply v3 migration
    await userDoc.ref.update({
      schemaVersion: 3,
      'privacySettings.isPublic': true,           // Default values
      'privacySettings.allowFriendRequests': true,
      'privacySettings.shareAnalytics': false,
      migratedAt: FieldValue.serverTimestamp(),
      migratedFrom: userData.schemaVersion || 1
    });
  }
}
```

**AI Learning Points:**
- Versioned schema approach for safe evolution
- Backward-compatible migration strategies
- Default value handling for new fields
- Migration tracking and audit trail

## 5. PERFORMANCE OPTIMIZATION PATTERNS

### **Query Optimization with Indexes**
```javascript
// INDEXES: Composite indexes for efficient queries (firestore.indexes.json)
{
  "indexes": [
    {
      "collectionGroup": "expenses",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "groupId", "order": "ASCENDING" },
        { "fieldPath": "date", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "expenses",
      "queryScope": "COLLECTION", 
      "fields": [
        { "fieldPath": "groupId", "order": "ASCENDING" },
        { "fieldPath": "category", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "groupActivities",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "groupId", "order": "ASCENDING" },
        { "fieldPath": "timestamp", "order": "DESCENDING" }
      ]
    }
  ]
}

// QUERY PATTERNS: Optimized for index usage
// Get group expenses with pagination
val query = firestore.collection("expenses")
    .whereEqualTo("groupId", groupId)           // First index field
    .orderBy("date", Query.Direction.DESCENDING) // Second index field
    .limit(20)                                  // Pagination limit

// Get categorized expenses
val categoryQuery = firestore.collection("expenses")
    .whereEqualTo("groupId", groupId)           // First index field
    .whereEqualTo("category", "FOOD")           // Second index field
    .orderBy("createdAt", Query.Direction.DESCENDING) // Third index field
```

**AI Learning Points:**
- Composite index design for complex queries
- Query pattern alignment with index structure
- Performance optimization through proper indexing
- Pagination strategies for large datasets

### **Data Batching Patterns**
```kotlin
// PATTERN: Batch operations for performance (data/repository/ExpenseRepository.kt)
suspend fun batchUpdateExpenses(updates: List<ExpenseUpdate>) = 
    performanceOptimizer.optimizeBatchOperation(
        items = updates,
        batchSize = 500 // Firestore batch limit
    ) { batch ->
        val firestoreBatch = firestore.batch()
        
        batch.forEach { update ->
            val docRef = firestore.collection("expenses").document(update.expenseId)
            firestoreBatch.update(docRef, update.changes)
        }
        
        firestoreBatch.commit().await()
    }

// PATTERN: Atomic transactions for data consistency
suspend fun transferExpenseOwnership(expenseId: String, newOwnerId: String) {
    firestore.runTransaction { transaction ->
        val expenseRef = firestore.collection("expenses").document(expenseId)
        val expense = transaction.get(expenseRef).toObject<Expense>()
        
        // Update expense
        transaction.update(expenseRef, mapOf(
            "paidBy" to newOwnerId,
            "modifiedBy" to auth.currentUser?.uid,
            "updatedAt" to FieldValue.serverTimestamp(),
            "version" to FieldValue.increment(1)
        ))
        
        // Log activity
        val activityRef = firestore.collection("groupActivities").document()
        transaction.set(activityRef, mapOf(
            "type" to "EXPENSE_TRANSFERRED",
            "groupId" to expense?.groupId,
            "expenseId" to expenseId,
            "timestamp" to FieldValue.serverTimestamp()
        ))
    }.await()
}
```

**AI Learning Points:**
- Batch operations for efficiency
- Atomic transactions for data consistency
- Version control for optimistic locking
- Activity logging within transactions

## 6. AI LEARNING OBJECTIVES FOR DATA MODELING

### **Schema Design Principles**
- **Denormalization**: Strategic data duplication for query performance
- **Aggregation**: Pre-calculated values for expensive operations
- **Versioning**: Safe schema evolution with backward compatibility
- **Security**: Access control integrated into data model design

### **Query Optimization Understanding**
- **Index Design**: Composite indexes for complex query patterns
- **Pagination**: Cursor-based navigation for large datasets
- **Batching**: Efficient bulk operations within platform limits
- **Caching**: Strategic data caching for frequently accessed information

### **Financial Data Specifics**
- **Audit Trails**: Complete history for financial record keeping
- **Split Calculations**: Complex business logic embedded in data structure
- **Settlement Tracking**: Debt resolution state management
- **Currency Handling**: Multi-currency support with proper precision

---

*AI Training Data for Firebase Schema & Data Models - Financial App Focus*
*Essential for understanding NoSQL data modeling in financial applications* 