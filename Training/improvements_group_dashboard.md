# Deep Dive: Group Dashboard & Expense List Improvements

This document analyzes the group detail screen, which serves as the main dashboard for a group, and provides suggestions for improving its functionality, performance, and user experience.

## 1. UI/UX (`GroupDetailScreen.kt`)

- **[UI/UX] Clear Balance Summary**
  - **Observation**: The dashboard shows a single "Your Balance" metric. This can be ambiguous. Does it mean the user is owed money or that they owe money?
  - **Suggestion**: Replace the single metric with a clearer summary. For example:
    - A green card showing "You are owed: $50" if the user has a positive balance.
    - A red card showing "You owe: $30" if the user has a negative balance.
    - A neutral card showing "You are all settled up" for a zero balance.

- **[UI/UX] Add Filtering and Sorting to Expense List**
  - **Observation**: The expense list is currently sorted by date. For groups with many expenses, this can be hard to navigate.
  - **Suggestion**: Add UI controls to allow the user to sort the expense list by amount (high-to-low or low-to-high) and filter by category or member. This would dramatically improve the usability of the expense history.

- **[UI/UX] Visualize Member Balances**
  - **Observation**: The screen lists group members but could provide more detail on their financial status within the group.
  - **Suggestion**: Enhance the member list to show each person's individual balance relative to the current user. For example: "Owes you $10" or "You owe $5".

## 2. Frontend Architecture (`GroupDetailViewModel.kt`)

- **[CRITICAL] Implement Balance Calculation Logic**
  - **Observation**: The ViewModel has a `TODO` for calculating the user's balance, which is the most critical missing feature on this screen.
  - **Suggestion**: Implement a robust balance calculation algorithm on the client side. The logic should iterate through every expense in the group and, for each expense, determine:
    1.  How much the current user paid.
    2.  How much the current user's share was (based on the split type).
    3.  The net amount for that expense is `(amount paid) - (share owed)`.
    4.  The final balance is the sum of these net amounts across all expenses.

- **[Performance] Cache Calculated Balances**
  - **Observation**: Calculating balances for a group with hundreds of expenses on the client every time the screen is viewed could be slow.
  - **Suggestion**: After calculating the balances for the first time, cache the results in the ViewModel. The cache should only be invalidated when the underlying expense or group data changes. This prevents redundant, heavy computations.

## 3. Backend & Data Layer

- **[Backend] Implement a `settlements` Collection**
  - **Observation**: The current data model doesn't appear to have a way to track when users pay each other back outside of creating a new expense.
  - **Suggestion**: Create a new top-level `settlements` collection in Firestore. A settlement document would record who paid whom, how much, and for which group. The balance calculation logic **must** incorporate these settlements to be accurate.

- **[Performance] Pre-Calculate Balances with Firebase Functions**
  - **Observation**: Client-side balance calculation can be resource-intensive.
  - **Suggestion**: For a highly scalable solution, move the balance calculation to the backend. Create a Firebase Function that is triggered whenever an expense or settlement is added, updated, or deleted for a group. This function would:
    1.  Recalculate the balances for all members in the affected group.
    2.  Save this information to a separate `group_summary` document in Firestore.
    3.  The client app would then only need to read this single, pre-computed `group_summary` document, making the group dashboard load almost instantly, regardless of the number of expenses.

- **[Security] Secure Group Data with Firestore Rules**
  - **Observation**: Access to group details and expenses must be restricted.
  - **Suggestion**: Implement Firestore security rules to ensure that a user can only read the details, expenses, and summary for a group if their `request.auth.uid` is present in that group's `memberIds` list. This prevents any user from accessing data from a group they do not belong to.
