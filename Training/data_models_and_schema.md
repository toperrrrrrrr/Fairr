# Data Models & Firestore Schema

This document outlines the target data schema for the Fairr application. It is designed for performance and scalability, leveraging denormalization to optimize for common read patterns.

## 1. Core Collections

### `users`
Stores public user profiles.
- **Document ID**: `userId` (from Firebase Auth)
- **Fields**:
  - `id`: `string`
  - `fullName`: `string`
  - `email`: `string`
  - `profileImageUrl`: `string` (optional)

### `groups`
Stores information about each expense-sharing group.
- **Document ID**: Auto-generated
- **Fields**:
  - `id`: `string`
  - `name`: `string`
  - `description`: `string`
  - `currency`: `string` (e.g., "USD")
  - `createdBy`: `string` (userId)
  - `memberIds`: `array` of `string` (list of userIds)
  - `memberInfo`: `map` (Denormalized map of userId to user's name and image URL for fast display of member lists).
  - `inviteCode`: `string`
  - `createdAt`: `timestamp`

### `expenses`
Stores individual expense records.
- **Document ID**: Auto-generated
- **Fields**:
  - `id`: `string`
  - `groupId`: `string`
  - `description`: `string`
  - `amount`: `double`
  - `date`: `timestamp`
  - `paidById`: `string` (userId)
  - `paidByName`: `string` (**Denormalized** for performance to avoid extra user profile lookups when displaying expense lists).
  - `splitType`: `string` (e.g., "EQUAL", "EXACT", "PERCENTAGE")
  - `splitBetween`: `map` (Details of the split, e.g., `{"userId1": 25.00, "userId2": 15.00}`)
  - `category`: `string`
  - `receiptUrl`: `string` (optional)

## 2. Supporting Collections

### `settlements` (New)
Tracks payments made between users to settle up their debts.
- **Purpose**: To provide a record of debt resolutions, which is critical for accurate balance calculations.
- **Document ID**: Auto-generated
- **Fields**:
  - `groupId`: `string`
  - `fromUserId`: `string`
  - `toUserId`: `string`
  - `amount`: `double`
  - `method`: `string` (e.g., "Cash", "Venmo")
  - `createdAt`: `timestamp`

## 3. Pre-computed Data Collections (for Performance)

### `group_summaries` (New)
Stores pre-calculated data for groups to enable fast dashboard loading.
- **Purpose**: To be calculated and updated by a Firebase Function whenever an expense or settlement is modified. The client app reads from this document instead of performing heavy calculations.
- **Document ID**: `groupId` (to match the group it summarizes)
- **Fields**:
  - `groupId`: `string`
  - `totalSpending`: `double`
  - `lastActivity`: `timestamp`
  - `balances`: `map` (A map of `userId` to their current balance, e.g., `{"userA": 50.25, "userB": -30.00}`)

## 4. Local Database Schema (Room)

The local Room database is used for offline caching, following the strategy outlined in the `architecture_deep_dive.md`.

- **`UserEntity`**: Caches user profiles.
- **`GroupEntity`**: Caches group details.
- **`ExpenseEntity`**: Caches expenses.

These entities will mirror the Firestore models, allowing the Repository layer to serve cached data first for a fast, offline-first user experience.
