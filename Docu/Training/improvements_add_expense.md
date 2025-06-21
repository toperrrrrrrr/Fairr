# Deep Dive: Add Expense Improvements

This document provides a detailed analysis of the "Add Expense" user flow, offering actionable suggestions for improvement across UI/UX, frontend architecture, and backend logic.

## 1. UI/UX (`AddExpenseScreen.kt`)

- **[UI/UX] Dynamic & User-Managed Categories**
  - **Observation**: The expense categories are currently hardcoded using `getDefaultCategories()`.
  - **Suggestion**: Allow users to create, edit, and delete their own categories. This could be managed at a global level or per-group. The UI should support this with a dedicated category management screen.

- **[UI/UX] Advanced Expense Splitting UI**
  - **Observation**: The screen supports multiple split types ("Equal", "Percentage", "Custom"). This is a complex feature that needs a highly intuitive UI.
  - **Suggestion**: For "Percentage" and "Custom" splits, provide a clear, interactive interface that shows the total amount, the amount already allocated, and the remaining balance. Use real-time validation to ensure the total split amount matches the expense amount before allowing the user to save.

- **[UI/UX] Enhanced OCR Interaction**
  - **Observation**: The app correctly suggests data from scanned receipts. 
  - **Suggestion**: Improve the UI for applying OCR suggestions. Instead of just a single prompt, consider allowing the user to tap on individual extracted fields (e.g., total amount, description) to populate the corresponding input fields. This gives the user more control.

## 2. Frontend Architecture (`AddExpenseViewModel.kt`)

- **[Architecture] Fetch Group-Specific Currency**
  - **Observation**: The `getCurrencySymbol()` function in the ViewModel contains a `TODO` and returns a hardcoded "$".
  - **Suggestion**: This is a critical fix. The ViewModel should fetch the currency from the current group's settings. The `groupId` is already available, so the `ExpenseRepository` or a new `GroupRepository` should expose a method to get group details, including its currency.

- **[Logic] Refine State Management**
  - **Observation**: The ViewModel uses a single `AddExpenseState` data class, which is good practice.
  - **Suggestion**: Ensure all transient UI events (like showing a snackbar) are handled through a `SharedFlow` or `Channel` (as is currently done with `_events`), keeping the `StateFlow` purely for representing the screen's state. This is a clean UDF pattern.

## 3. Backend & Data Layer (`ExpenseRepositoryImpl.kt`)

- **[Performance] Solve the N+1 Query Problem**
  - **Observation**: The `getExpensesByGroupId` function fetches all expenses and then makes a separate Firestore query for *each expense* to get the name of the user who paid. This is highly inefficient and will lead to slow load times and high costs as the number of expenses grows.
  - **Suggestion**: **Denormalize** the data. When an expense is created, store the `paidByName` (the display name of the user) directly within the expense document. This eliminates the need for subsequent lookups, resolving the N+1 problem with a single, efficient query.

- **[Backend] Use Transactions for Atomic Operations**
  - **Observation**: The `addExpense` logic needs to perform at least two separate writes: creating the new expense document and updating the total balance in the corresponding group document.
  - **Suggestion**: Wrap these operations in a **Firestore transaction**. This guarantees that both writes succeed or fail together. It prevents data inconsistency, such as an expense being added without the group total being updated.

- **[Backend] Server-Side Expense Splitting Logic**
  - **Observation**: The logic for calculating how an expense is split between users is likely handled on the client.
  - **Suggestion**: For complex operations like this, consider moving the logic to a **Firebase Function**. The client would pass the expense details and split type, and the function would perform the calculations and database writes. This ensures the logic is consistent, secure, and not dependent on the client's state.

- **[Security] Enforce Strict Firestore Security Rules**
  - **Observation**: The repository manually checks if a user is part of a group before adding an expense. While good, this is not a substitute for proper security rules.
  - **Suggestion**: Implement Firestore security rules that enforce this at the database level. Rules should:
    - Only allow a user to create an expense if their `request.auth.uid` is in the group's `memberIds` list.
    - Only allow users within a group to read the expenses for that `groupId`.
    - Prevent users from modifying expenses created by others (unless specific logic allows for it).
