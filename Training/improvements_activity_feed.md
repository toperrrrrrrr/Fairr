# Improvements Deep Dive: Group Activity Feed

This document outlines the design and implementation of the Group Activity Feed, a feature that provides a chronological log of all significant actions within a group.

## 1. User Stories

- **As a group member**, I want to see a history of all activities in my group, such as when new expenses were added or who joined the group, so I can stay up-to-date.
- **As a group member**, I want to easily track when settlements were made to understand how balances have changed over time.
- **As a user**, I want a clear and simple log to resolve any confusion or disputes about past expenses or payments.

## 2. UI/UX Design

- **Entry Point**: There will be an "Activity" tab or section within the main screen for each group.
- **Chronological List**: The feed will be a vertically scrolling list, with the most recent events at the top.
- **Activity Items**: Each item in the feed will be designed for quick scanning and will contain:
  - An icon representing the type of activity (e.g., a receipt icon for a new expense, a handshake icon for a settlement).
  - A concise, human-readable description (e.g., "**You** added **'Groceries'** for **$50.00**").
  - A timestamp indicating when the event occurred (e.g., "Yesterday" or "June 10").
- **Grouping**: Activities that occur on the same day can be grouped under a single date header for better readability.

## 3. Backend & Firebase Implementation

This feature will be powered by a new sub-collection and populated by Firebase Functions.

### `activity_logs` Sub-collection
- **Path**: `groups/{groupId}/activity_logs/{activityId}`
- **Purpose**: To store a persistent, immutable log of all important events related to a specific group.
- **Schema**: See the `firestore_collections.md` document for the detailed schema.

### Populating the Feed via Firebase Functions

The activity feed will be populated automatically by Firebase Functions that are triggered by events in other collections. This ensures the log is reliable and cannot be tampered with by clients.

- **Trigger on New Expense**: An `onCreate` function on the `expenses` collection will create a new document in the `activity_logs` sub-collection for the corresponding group. The log's description will be something like `"[User Name] added '[Expense Description]' for [Amount]."`
- **Trigger on New Settlement**: An `onCreate` function on the `settlements` collection will create an activity log entry like `"[User A] paid [User B] [Amount]."`
- **Trigger on Member Joining**: When a user successfully accepts an invite, the `acceptGroupInvite` function will also be responsible for creating an activity log entry like `"[New Member Name] joined the group."`
- **Trigger on Member Leaving/Removal**: Similarly, the function that handles removing a member will create an entry like `"[Member Name] left the group."`

## 4. Security Considerations

- **Read-Only for Clients**: The `activity_logs` sub-collection should be read-only for clients. All write operations must be handled exclusively by trusted backend Firebase Functions. This prevents users from creating fake or misleading activity log entries.
- **Firestore Rules for `activity_logs`**:
  ```javascript
  match /groups/{groupId}/activity_logs/{activityId} {
    // Only allow reads for members of the parent group.
    allow read: if request.auth.uid in get(/databases/$(database)/documents/groups/$(groupId)).data.memberIds;
    // Disallow all client-side writes.
    allow write: if false;
  }
  ```

## 5. Frontend Architecture

- **`ActivityViewModel`**: A new ViewModel will be responsible for the logic of the activity feed screen.
- **`ActivityRepository`**: A new repository will be created to handle fetching the paginated list of activity log documents from the `groups/{groupId}/activity_logs` sub-collection.
- **UI State**: The `ActivityUiState` will hold the list of activity items, as well as loading and empty state information.
- **Pagination**: To ensure performance, the frontend will fetch the activity logs in paginated chunks (e.g., 20-30 items at a time) rather than all at once.
