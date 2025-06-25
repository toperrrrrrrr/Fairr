# Data Modeling Principles & Strategy

This document outlines the high-level strategies and principles governing the data models for the Fairr application. It explains *why* our data is structured the way it is.

For the definitive, concrete schema for every collection, please refer to our single source of truth:
**[Firestore Collections: Single Source of Truth](./firestore_collections.md)**

---

## 1. Firestore Data Modeling Strategy

Our Firestore schema is designed with two primary goals: **performance** and **scalability**. We follow several key principles to achieve this.

### Principle 1: Denormalization for Read Performance

We prioritize fast read operations, as these are the most common in the app (e.g., loading a group's dashboard). To avoid slow, complex, and costly client-side joins, we strategically denormalize data.

- **Example**: When an expense is created, we store the `paidByName` directly on the expense document. This prevents the client from needing to fetch the profile for every single user for every single expense in a list, which would be a classic N+1 query problem.

### Principle 2: Backend-Managed Data for Integrity

Any data that is critical for financial calculations or that summarizes other data must be calculated and managed by a trusted backend environment (Firebase Functions), not the client.

- **Example**: The `group_summaries` collection is read-only for clients. It is populated by a Firebase Function that triggers whenever an expense or settlement is added. This ensures that the user balances displayed on the dashboard are always authoritative and cannot be tampered with.

### Principle 3: Data Structure Optimized for Security Rules

Our data is structured to make writing effective and secure Firestore Security Rules as simple as possible.

- **Example**: Every group document contains a `memberIds` array. This allows us to write a simple rule that grants access to a group and its sub-collections (like expenses) only if the requesting user's `uid` is present in that array.

---

## 2. Local Database Strategy (Room)

The local Room database is a critical part of our architecture, enabling a fast, offline-first user experience.

- **Purpose**: To serve as a local cache of the Firestore data. The UI will always read from the repository, which will first attempt to provide data from the Room cache for an instantaneous response, while simultaneously fetching fresh data from Firestore in the background.
- **Schema**: The Room database schema (e.g., `UserEntity`, `GroupEntity`, `ExpenseEntity`) will generally mirror the structure of the corresponding Firestore collections.
- **Synchronization**: The `Repository` layer is responsible for all synchronization logic, ensuring that the local cache is kept up-to-date with the remote database.
