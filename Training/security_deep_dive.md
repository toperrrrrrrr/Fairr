# Security Deep Dive

This document outlines the security architecture and best practices for the Fairr application. The primary goal is to protect user data integrity and privacy by implementing security at every layer.

## 1. The Principle of Least Privilege

Our security model is based on the principle of least privilege: a user should only have access to the data and operations that are absolutely necessary for their legitimate tasks. This is primarily enforced on the backend via Firestore Security Rules.

## 2. Firestore Security Rules

Firestore Security Rules are the most critical component of our security posture. They are not optional; they are the primary defense against unauthorized data access. The client is considered untrusted, and all access control must be defined here.

Below are the target rules for each collection. These rules must be deployed to Firebase before the app goes into production.

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    // Users can only read and write their own profile.
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }

    // A user can read a group's document only if their UID is in the memberIds list.
    // A user can only create a group if they are listed as a member.
    // Updates are only allowed by existing members.
    match /groups/{groupId} {
      allow read: if request.auth.uid in resource.data.memberIds;
      allow create: if request.auth.uid in request.resource.data.memberIds;
      allow update: if request.auth.uid in resource.data.memberIds;
      // Deletion should be handled by a secure Firebase Function to ensure all sub-collections are cleaned up.
      allow delete: if false; 
    }

    // A user can access an expense only if they are a member of the group it belongs to.
    // This requires a 'get' call to check the parent group document, which is a highly secure pattern.
    match /expenses/{expenseId} {
      allow read, create, update: if request.auth.uid in get(/databases/$(database)/documents/groups/$(request.resource.data.groupId)).data.memberIds;
      // Deletion should be handled by a secure Firebase Function.
      allow delete: if false;
    }

    // A user can access a settlement only if they are a member of the group it belongs to.
    match /settlements/{settlementId} {
        allow read, create: if request.auth.uid in get(/databases/$(database)/documents/groups/$(request.resource.data.groupId)).data.memberIds;
        // Settlements should be immutable.
        allow update, delete: if false;
    }

    // Group summaries are read-only for clients. They are only written to by backend functions.
    match /group_summaries/{groupId} {
        allow read: if request.auth.uid in get(/databases/$(database)/documents/groups/$(groupId)).data.memberIds;
        allow write: if false; // Only backend can write
    }
  }
}
```

## 3. Secure Backend Logic with Firebase Functions

Any logic that is sensitive or critical for data integrity must be executed on a secure backend, not on the client.

- **Why?** Client-side code can be reverse-engineered, tampered with, or bypassed. Placing logic on the server ensures it is always executed as intended.
- **Examples in Fairr**:
  - **Debt Simplification**: The algorithm to calculate settlement plans involves financial calculations that must be accurate and authoritative. This will be a callable Firebase Function.
  - **Invite Code Generation**: To prevent abuse, invite codes should be generated and validated on the server.
  - **Updating `group_summaries`**: All writes to the pre-computed summary documents will be performed by a Firebase Function that triggers on changes to `expenses` or `settlements`.

## 4. Input Validation

We must employ a two-layer validation strategy:

1.  **Client-Side Validation**: Provide immediate feedback to the user for a good UX (e.g., checking if an email has a valid format, if an expense amount is positive). This is a UX feature, **not a security feature**.
2.  **Server-Side Validation**: This is the real security check. Firestore Security Rules must validate incoming data to ensure it conforms to the expected schema and constraints (e.g., checking that an expense amount is a number, that a description is not excessively long, that a user isn't adding themselves to a group they weren't invited to).

## 5. Authentication & Session Management

- **Firebase Authentication**: We will rely on the Firebase Auth SDK to handle all aspects of user authentication, including secure token generation, storage, and refresh.
- **Token Management**: The client-side code should not manually store or manage auth tokens. This is handled securely by the SDK.
- **Secure Sign-in**: Continue to promote secure sign-in methods like Google Sign-In, which reduces the user's password burden and leverages Google's security infrastructure.
