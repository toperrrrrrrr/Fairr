# Firestore Collections: Single Source of Truth

This document provides a comprehensive overview of the Firestore database schema for the Fairr application. It is the definitive guide for all collections, detailing their purpose, structure, and implementation status.

---

## Part 1: Core Implemented & Defined Collections

These collections form the foundational data model of the application. They are either fully implemented or have a clearly defined schema and are ready for implementation.

### 1. `users/{userId}`
- **Purpose**: Stores public-facing profile information for each registered user.
- **Key Fields**:
  - `userId` (string): The user's unique Firebase Auth UID.
  - `displayName` (string): The user's chosen display name.
  - `email` (string): The user's registration email.
  - `photoUrl` (string, optional): A URL to the user's profile picture.
- **Status**: **Implemented**.

### 2. `groups/{groupId}`
- **Purpose**: Contains the core information for each expense-sharing group.
- **Key Fields**:
  - `groupId` (string): The unique ID for the group.
  - `groupName` (string): The name of the group.
  - `description` (string, optional): A brief description of the group.
  - `currency` (string): The default currency for the group (e.g., "USD").
  - `memberIds` (array of strings): A list of `userId`s for all members of the group. This is critical for security rules.
- **Status**: **Implemented**.

### 3. `expenses/{expenseId}`
- **Purpose**: Stores the details for every individual expense record.
- **Key Fields**:
  - `expenseId` (string): The unique ID for the expense.
  - `groupId` (string): The ID of the group this expense belongs to.
  - `description` (string): The name or description of the expense (e.g., "Groceries").
  - `amount` (number): The total cost of the expense.
  - `paidById` (string): The `userId` of the member who paid for the expense.
  - `paidByName` (string): The denormalized display name of the payer for efficient UI rendering.
  - `splitType` (string): The method of splitting ("EQUAL", "PERCENTAGE", "CUSTOM").
  - `participants` (map): Defines how the expense is split among members.
  - `timestamp` (timestamp): The date and time the expense was recorded.
- **Status**: **Implemented** (for basic "EQUAL" splits).

### 4. `settlements/{settlementId}`
- **Purpose**: Creates an immutable, historical record of a payment made between two users to settle a debt.
- **Key Fields**:
  - `settlementId` (string): The unique ID for the settlement transaction.
  - `groupId` (string): The ID of the group where the settlement occurred.
  - `fromUserId` (string): The `userId` of the user who made the payment.
  - `toUserId` (string): The `userId` of the user who received the payment.
  - `amount` (number): The amount of money settled.
  - `timestamp` (timestamp): The date and time the settlement was recorded.
- **Status**: **Defined & Planned** (Awaiting implementation of the settlement feature).

### 5. `group_summaries/{groupId}`
- **Purpose**: A denormalized, pre-computed document for storing the current balance state of a group. This is designed for high-performance dashboard reads and will be managed by backend functions.
- **Key Fields**:
  - `groupId` (string): The ID of the group being summarized.
  - `totalSpending` (number): The sum of all expenses in the group.
  - `balances` (map): A map where each key is a `userId` and the value is their current balance (positive if they are owed money, negative if they owe money).
  - `lastUpdated` (timestamp): When the summary was last calculated.
- **Status**: **Defined & Planned** (To be implemented alongside backend Firebase Functions).

---

## Part 2: Future Planned Collections

These collections are required to support planned features that are essential for a complete user experience. Their schemas are proposed here and will be finalized in their respective feature-specific design documents.

### 6. `invites/{inviteId}`
- **Purpose**: To securely manage pending invitations for users to join a group.
- **Proposed Key Fields**:
  - `inviteId` (string): The unique ID for the invitation.
  - `groupId` (string): The ID of the group the user is invited to.
  - `groupName` (string): The denormalized name of the group.
  - `inviterId` (string): The `userId` of the member who sent the invitation.
  - `inviteeEmail` (string): The email address of the person being invited.
  - `status` (string): The current state of the invite (`pending`, `accepted`, `declined`).
  - `expiresAt` (timestamp): A timestamp for when the invite will expire.
- **Needed for**: Advanced Group Management.

### 7. `notifications/{userId}/user_notifications/{notificationId}`
- **Purpose**: To store a list of notifications for each user in a scalable way using a sub-collection.
- **Proposed Key Fields**:
  - `notificationId` (string): The unique ID for the notification.
  - `type` (string): The type of event (`new_expense`, `settlement_made`, `group_invite`, `user_joined`).
  - `message` (string): The human-readable notification text.
  - `isRead` (boolean): `true` if the user has viewed the notification.
  - `relatedGroupId` (string): The ID of the group related to this notification.
  - `timestamp` (timestamp): When the notification was generated.
- **Needed for**: Notifications System.

### 8. `activity_logs/{groupId}/group_activities/{activityId}`
- **Purpose**: To store a chronological log of all significant events within a group for the "Activity" feed.
- **Proposed Key Fields**:
  - `activityId` (string): The unique ID for the activity log entry.
  - `actorId` (string): The `userId` of the user who performed the action.
  - `actorName` (string): The denormalized name of the actor.
  - `type` (string): The type of activity (`expense_added`, `expense_edited`, `member_joined`, `settlement_recorded`).
  - `description` (string): A generated description of the event (e.g., "John Doe added 'Groceries' for $50").
  - `timestamp` (timestamp): When the activity occurred.
- **Needed for**: Group Activity Feed.
