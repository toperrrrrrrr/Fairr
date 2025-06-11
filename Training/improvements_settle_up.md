# Deep Dive: Settle Up & Debt Simplification Improvements

This document provides a detailed analysis of the "Settle Up" user flow. It focuses on the critical missing logic for calculating debts and provides a full-stack roadmap for implementation.

## 1. UI/UX (`SettlementScreen.kt`)

- **[UI/UX] Dynamic Data Display**
  - **Observation**: The screen uses static, hardcoded sample data. 
  - **Suggestion**: The screen must be connected to a ViewModel that provides a dynamically calculated list of required payments to settle all debts within the group.

- **[UI/UX] Improved Payment Recording**
  - **Observation**: The payment dialog is basic and only asks for a payment method.
  - **Suggestion**: Enhance the dialog to allow the user to add a note or a transaction reference. Also, allow for partial payments by letting the user edit the settlement amount.

- **[UI/UX] Confirmation and Feedback**
  - **Observation**: There is no confirmation after a settlement is recorded.
  - **Suggestion**: After a user records a payment, display a confirmation message (e.g., a snackbar) and update the UI to reflect the new balances, perhaps by removing the settled debt from the list.

## 2. Frontend Architecture (New `SettlementViewModel`)

- **[Architecture] Create a `SettlementViewModel`**
  - **Observation**: The `SettlementScreen` currently has no ViewModel and manages its own state, which is not scalable or maintainable.
  - **Suggestion**: Create a `SettlementViewModel` responsible for:
    1.  Orchestrating the calculation of the settlement plan.
    2.  Holding the UI state (the list of required payments).
    3.  Calling the repository to record a new settlement when the user confirms a payment.

- **[Logic] Fetch Calculated Settlement Plan**
  - **Observation**: The core logic is missing.
  - **Suggestion**: The ViewModel should not perform the settlement calculation itself. Instead, it should call a service or repository method (e.g., `settlementService.getSettlementPlan(groupId)`) to get the list of required transactions.

## 3. Backend & Data Layer (New `SettlementService`)

- **[CRITICAL] Implement Debt Simplification Algorithm**
  - **Observation**: The app needs a way to calculate the minimum number of payments required to settle all debts.
  - **Suggestion**: This is the most important task. The algorithm should be implemented in a new `SettlementService` or, preferably, a Firebase Function.
    1.  **Calculate Balances**: For each member in the group, calculate their final balance: `(total amount they paid) - (total amount of their shares)`. This should also include any past settlements.
    2.  **Identify Debtors and Creditors**: Separate the members into two lists: those with a negative balance (debtors) and those with a positive balance (creditors).
    3.  **Generate Transactions**: Match debtors with creditors to create a list of payment transactions that clears all balances. For example, a debtor who owes $50 can pay a creditor who is owed $75, which settles the debtor and leaves the creditor waiting for $25 from someone else. The goal is to create the fewest possible transactions.

- **[Backend] Create a `settlements` Collection in Firestore**
  - **Observation**: There is no data model for recording settlements.
  - **Suggestion**: Create a new top-level `settlements` collection. Each document should store:
    - `groupId`: The ID of the group.
    - `fromUserId`: The UID of the user who paid.
    - `toUserId`: The UID of the user who received the payment.
    - `amount`: The amount settled.
    - `method`: The payment method (e.g., "Cash").
    - `note`: An optional user-provided note.
    - `createdAt`: A timestamp.

- **[Backend] Use a Firebase Function for Scalability**
  - **Observation**: Calculating the settlement plan can be complex.
  - **Suggestion**: Create a callable Firebase Function named `calculateSettlementPlan`. The client app would call this function with a `groupId`. The function would then perform the debt simplification algorithm and return the list of required payments. This keeps the heavy logic on the server, ensuring it's fast, secure, and consistent for all users.

- **[Security] Secure Settlement Operations**
  - **Suggestion**: Implement Firestore security rules to:
    - Only allow a user to create a settlement document if they are a member of the group and are either the sender or receiver of the funds.
    - Only allow members of a group to read settlement data for that group.
