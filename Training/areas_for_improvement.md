# Areas for Improvement

This document outlines potential areas for improvement in the Fairr application, categorized by domain. It serves as a high-level overview, with more detailed implementation plans available in the specific deep-dive documents.

## 1. UI/UX Design

- **Advanced Expense Splitting UI**: Design a more intuitive and powerful interface for custom and percentage-based splits.
- **Onboarding Flow**: Enhance the first-time user experience with a guided walkthrough of key features and an optional skip button.
- **Accessibility (A11y)**: Ensure all components are fully accessible by providing content descriptions, supporting dynamic font sizes, and testing with screen readers.
- **Feedback & Error States**: Provide clear, contextual feedback for both successful and failed operations (e.g., using snackbars with informative messages).
- **Empty States**: Design thoughtful empty state screens (e.g., for a new group with no expenses) to guide the user on what to do next.
- **Theming**: Introduce user-customizable themes, including a dark mode and high-contrast options.

## 2. Backend & Performance

- **Implement Debt Simplification**: The core logic for calculating the simplest settlement plan is missing. This needs to be implemented, preferably in a Firebase Function, to make the "Settle Up" feature functional.
- **Solve N+1 Query Problem**: The current method of fetching user data for each expense individually is a major performance bottleneck. Denormalizing the data by storing `paidByName` in the expense document is critical.
- **Offload Logic to Firebase Functions**: Move complex or sensitive operations like balance calculations, invite code generation, and receipt processing to the backend to improve client performance and security.
- **Use Firestore Transactions**: Ensure data integrity by wrapping all multi-write operations (like adding an expense) in atomic transactions.
- **Pre-computed Summaries**: For instant dashboard loading, use Firebase Functions to maintain a `group_summary` document with pre-calculated balances for each group.

## 3. Architecture & Code Quality

- **Implement Strict Firestore Security Rules**: This is a top-priority security task to prevent unauthorized data access. Rules must be written to ensure users can only access data for groups they are members of.
- **Enforce Unidirectional Data Flow (UDF)**: While partially adopted, all ViewModels should be refactored to expose a single, immutable UI state object (`StateFlow`) and handle one-off actions via an events flow (`SharedFlow`).
- **Complete Offline Caching Strategy**: The plan to use Room for caching is sound, but it needs to be fully implemented. The repository layer should be responsible for the synchronization logic between the local cache and Firestore.
- **Dependency Injection Refinement**: Ensure all external dependencies, especially Coroutine `Dispatchers`, are injected via Hilt to improve testability.
- **Improve Error Handling**: Standardize error handling across the app, using a combination of `Result` wrappers in the data layer and sealed `UiState` classes in the presentation layer.

## 4. Feature Enhancements

- **Forgot Password Flow**: A standard, essential feature for user authentication.
- **User-Managed Categories**: Allow users to create, edit, and delete their own expense categories for better personalization.
- **Receipt Management**: Build out the full feature for uploading, viewing, and managing expense receipts using Firebase Storage.
- **Search & Filtering**: Implement robust search functionality within groups and a global search for expenses or groups.
