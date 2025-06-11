# Data Models and Schema

This document outlines the data models and schema used in the Fairr application for both remote (Firebase Firestore) and local (Room) storage.

## Firebase Firestore Schema

Firestore is used as the primary remote database. The schema is designed to be scalable and support real-time updates. The models below are based on the data classes found in the codebase.

### `users` collection

Stores public information about users. Based on the `UserProfile` data class.

- **Document ID**: `userId` (from Firebase Authentication)
- **Fields**:
  - `id`: `string` - The user's unique ID.
  - `fullName`: `string` - The user's full name.
  - `email`: `string` - The user's email address.
  - `profileImageUrl`: `string` (optional) - URL to the user's profile picture.
  - `phoneNumber`: `string` (optional) - The user's phone number.
  - `joinDate`: `string` - The date the user joined.
  - `totalGroups`: `int` - The number of groups the user is a member of.
  - `totalExpenses`: `int` - The total number of expenses created by the user.
  - `isEmailVerified`: `boolean` - Flag indicating if the user's email is verified.

### `groups` collection

Stores information about each expense group. Based on the `Group` data class.

- **Document ID**: Auto-generated ID
- **Fields**:
  - `id`: `string` - The unique ID of the group.
  - `name`: `string` - The name of the group.
  - `description`: `string` - A short description of the group.
  - `currency`: `string` - The default currency for the group (e.g., "PHP").
  - `createdBy`: `string` - The `userId` of the user who created the group.
  - `members`: `array` of `object` - A list of `GroupMember` objects.
  - `inviteCode`: `string` - A code to invite new members to the group.
  - `createdAt`: `long` - The timestamp when the group was created.

### `expenses` collection

Stores individual expense records. Based on the `Expense` data class.

- **Document ID**: Auto-generated ID
- **Fields**:
  - `id`: `string` - The unique ID of the expense.
  - `groupId`: `string` - The ID of the group this expense belongs to.
  - `description`: `string` - A description of the expense.
  - `amount`: `double` - The total amount of the expense.
  - `currency`: `string` - The currency of the expense (e.g., "USD").
  - `date`: `timestamp` - The date and time the expense was recorded.
  - `paidBy`: `string` - The `userId` of the member who paid for the expense.
  - `paidByName`: `string` - The name of the member who paid.
  - `splitBetween`: `array` of `object` - A list of `ExpenseSplit` objects detailing how the expense is split.
  - `category`: `enum` - The category of the expense (e.g., `OTHER`).
  - `notes`: `string` - Additional notes about the expense.
  - `attachments`: `array` of `string` - A list of URLs to attachments (e.g., receipts).

## Room Database Schema (Local)

The local Room database is used for offline caching. The schema mirrors the Firestore collections to provide a seamless offline experience.

- **`UserEntity`**: Caches user profile information.
- **`GroupEntity`**: Caches group details.
- **`ExpenseEntity`**: Caches expenses for offline viewing and editing.

Each entity will have fields corresponding to the Firestore documents, with annotations for Room (e.g., `@Entity`, `@PrimaryKey`). This allows the app to function offline and sync with Firestore when a connection is available.
