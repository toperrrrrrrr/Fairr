# Technical Improvement Roadmap

This document provides a prioritized, high-level roadmap for the development of the Fairr app. It consolidates the most critical findings from the deep-dive analyses of the core user flows.

## Phase 1: Critical Functionality & Bug Fixes

*This phase focuses on implementing the core missing features and fixing clear bugs that prevent the app from being fully functional.*

1.  **Implement Debt Simplification Algorithm**
    - **Goal**: Calculate the minimum number of payments required to settle all debts in a group.
    - **Area**: Backend (`SettlementService` or Firebase Function).
    - **Justification**: This is the most critical missing piece of the app's core logic. The "Settle Up" feature is non-functional without it.

2.  **Implement Accurate Balance Calculation**
    - **Goal**: Correctly calculate and display each user's balance within a group.
    - **Area**: Frontend (`GroupDetailViewModel`).
    - **Justification**: Fixes the `TODO` in the group dashboard. This is essential for users to understand their financial status.

3.  **Create `settlements` Firestore Collection**
    - **Goal**: Define and use a new collection to record when users pay each other back.
    - **Area**: Backend (Firestore Data Model).
    - **Justification**: The data model is incomplete without a way to track settlements, making accurate balance calculation impossible.

4.  **Fix Hardcoded Currency Symbol**
    - **Goal**: Fetch the correct currency symbol from group settings instead of using a hardcoded "$".
    - **Area**: Frontend (`AddExpenseViewModel`).
    - **Justification**: This is a straightforward bug fix required for the app to support multiple currencies.

## Phase 2: Performance & Reliability

*This phase focuses on optimizing the app for speed and ensuring data consistency, especially as the amount of data grows.*

1.  **Solve the N+1 Query Problem in Expense Loading**
    - **Goal**: Denormalize data by storing `paidByName` directly in each expense document.
    - **Area**: Backend (`ExpenseRepositoryImpl`).
    - **Justification**: This is a major performance bottleneck that will cause slow load times and high Firestore costs. Fixing it is crucial for scalability.

2.  **Use Firestore Transactions for Atomic Writes**
    - **Goal**: Ensure that multi-step database operations (like adding an expense and updating the group total) succeed or fail together.
    - **Area**: Backend (`ExpenseRepositoryImpl`, `GroupService`).
    - **Justification**: Prevents data corruption and ensures the database remains in a consistent state.

3.  **Offload Calculations to Firebase Functions**
    - **Goal**: Move heavy logic like balance calculation and invite code generation to the backend.
    - **Area**: Backend (Firebase Functions).
    - **Justification**: Improves client performance, enhances security, and ensures business logic is consistent across all clients.

## Phase 3: Architecture & Security

*This phase focuses on improving the codebase's structure, maintainability, and security posture.*

1.  **Implement Strict Firestore Security Rules**
    - **Goal**: Write and deploy comprehensive security rules for all collections (`groups`, `expenses`, `users`, `settlements`).
    - **Area**: Backend (Firestore Security).
    - **Justification**: This is a critical security task to prevent unauthorized data access and manipulation.

2.  **Refactor ViewModels to Use a Single State Object**
    - **Goal**: Consolidate multiple `State` properties in ViewModels into a single data class.
    - **Area**: Frontend (All ViewModels).
    - **Justification**: Aligns with modern Unidirectional Data Flow (UDF) principles, making state management more predictable and easier to debug.

3.  **Decouple UI from Logic in Settlement Screen**
    - **Goal**: Create a `SettlementViewModel` to handle the logic for the `SettlementScreen`.
    - **Area**: Frontend (`SettlementScreen`).
    - **Justification**: Improves code structure and testability by separating concerns.

## Phase 4: High-Impact UI/UX Enhancements

*This phase focuses on the most impactful features that will significantly improve the user experience.*

1.  **Implement the 'Forgot Password' Flow**
    - **Goal**: Allow users to reset their password.
    - **Area**: Frontend/Backend (Authentication Flow).
    - **Justification**: A standard, essential feature for any app with user accounts.

2.  **Build a Dynamic UI for Expense Splitting**
    - **Goal**: Create a highly interactive and intuitive UI for handling custom and percentage-based expense splits.
    - **Area**: Frontend (`AddExpenseScreen`).
    - **Justification**: The current implementation is basic; a better UI would make one of the app's most powerful features much more usable.

3.  **Allow User-Managed Expense Categories**
    - **Goal**: Let users create, edit, and delete their own expense categories.
    - **Area**: Full Stack.
    - **Justification**: Greatly enhances personalization and makes the app more flexible for different user needs.
