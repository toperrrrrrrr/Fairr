# High-Level Project Goals & Areas for Improvement

This document serves as a high-level, categorized summary of our project's goals and identified areas for improvement. It is a strategic overview of *what* we aim to achieve.

For the official, prioritized, and phased implementation plan, please refer to the main planning document:
**[Technical Roadmap](./technical_roadmap.md)**

---

## 1. UI/UX Design

- **Advanced Expense Splitting UI**: Design a more intuitive and powerful interface for custom and percentage-based splits.
- **Onboarding Flow**: Enhance the first-time user experience with a guided walkthrough of key features.
- **Accessibility (A11y)**: Ensure all components are fully accessible (content descriptions, dynamic fonts, etc.).
- **Feedback & Error States**: Provide clear, contextual feedback for all user operations.
- **Empty States**: Design thoughtful empty state screens to guide users.
- **Theming**: Introduce user-customizable themes, including dark mode.

## 2. Backend & Performance

- **Implement Debt Simplification**: The core logic for the "Settle Up" feature. This is a top priority.
- **Solve N+1 Query Problem**: Denormalize user data into expense documents to improve performance.
- **Offload Logic to Firebase Functions**: Move sensitive or complex logic to the backend for security and performance.
- **Use Firestore Transactions**: Ensure data integrity for all multi-write operations.
- **Pre-computed Summaries**: Use Firebase Functions to maintain pre-calculated group balances for instant dashboard loading.

## 3. Architecture & Code Quality

- **Implement Strict Firestore Security Rules**: A top-priority security task to prevent unauthorized data access.
- **Enforce Unidirectional Data Flow (UDF)**: Ensure all ViewModels follow a strict UDF pattern with a single state object.
- **Complete Offline Caching Strategy**: Fully implement the Room database caching and synchronization logic.
- **Dependency Injection Refinement**: Inject all external dependencies (including `Dispatchers`) via Hilt for testability.
- **Improve Error Handling**: Standardize error handling across all layers of the application.

## 4. Feature Enhancements

- **Forgot Password Flow**: Implement a standard password reset feature.
- **User-Managed Categories**: Allow users to create and manage their own expense categories.
- **Receipt Management**: Build the full feature for uploading and managing expense receipts.
- **Search & Filtering**: Implement robust search and filtering capabilities for expenses and groups.
