# Improvements Deep Dive: Notifications System

This document outlines the design and implementation of a comprehensive notification system for the Fairr app. This system will include both in-app notifications and push notifications to keep users informed of relevant activity.

## 1. User Stories

- **As a user**, I want to receive a push notification when someone adds me to a new group so I can take action immediately.
- **As a user**, I want to be notified when a new expense is added to one of my groups.
- **As a user**, I want to receive a notification when another member settles a debt with me.
- **As a user**, I want to see a list of all my past notifications within the app.
- **As a user**, I want to be able to control which types of notifications I receive.

## 2. UI/UX Design

- **Push Notifications**: Standard OS-level push notifications will be used. When tapped, they should deep-link the user directly to the relevant screen (e.g., a tap on a "new expense" notification opens the specific group's dashboard).
- **In-App Notification Center**: A dedicated "Notifications" screen (accessible via a bell icon in the app bar) will display a chronological list of all notifications for the user.
- **Unread Indicators**: An indicator (e.g., a red dot) will appear on the bell icon when there are unread notifications.
- **Notification Items**: Each item in the list will show an icon corresponding to the notification type, a clear message, and a timestamp.

## 3. Backend & Firebase Implementation

This system will be powered by **Firebase Cloud Messaging (FCM)** and **Firebase Functions**.

### `notifications` Sub-collection
- **Path**: `users/{userId}/notifications/{notificationId}`
- **Purpose**: To store a persistent record of all notifications sent to a user. This collection will populate the in-app notification center.
- **Schema**: See the `firestore_collections.md` document for the detailed schema.

### Firebase Cloud Messaging (FCM) Setup
1.  **FCM Token Management**: When a user logs in, the client app will get the unique FCM registration token for that device. This token must be saved to the user's private profile or a dedicated `devices` collection (e.g., `users/{userId}/devices/{deviceId}`).
2.  **Triggering Notifications**: We will use Firebase Functions to send notifications. The client will **never** trigger a push notification directly.

### Example Flow: New Expense Notification
1.  A user adds a new expense. This writes a new document to the `expenses` collection.
2.  A **Firebase Function** is triggered by this `onCreate` event in the `expenses` collection.
3.  The function's logic will:
    a. Get the list of `memberIds` from the parent group document.
    b. For each member (excluding the person who added the expense):
        i. Create a new document in their `users/{memberId}/notifications` sub-collection.
        ii. Retrieve their FCM device token(s).
        iii. Construct a notification payload (title, body, deep-link data).
        iv. Use the Firebase Admin SDK to send the push notification via FCM to that user's device(s).

## 4. Security Considerations

- **Server-Side Sending**: All push notifications must be sent from a trusted server environment (Firebase Functions). The client app will not have the credentials to send FCM messages, preventing abuse.
- **Targeted Notifications**: The backend logic must be carefully written to ensure notifications are only sent to the relevant users (e.g., members of the group where the event occurred).
- **Data in Payload**: Do not include sensitive information in the notification payload itself. The payload should contain just enough information to display the message and a deep-link identifier. The app will then fetch the full data securely upon opening.

## 5. Frontend Architecture

- **`FirebaseMessagingService`**: A custom service will be implemented on Android to receive and handle incoming push notifications when the app is in the background or killed.
- **`NotificationsViewModel`**: This ViewModel will be responsible for fetching the list of notifications from the `users/{userId}/notifications` collection to populate the in-app notification center.
- **`NotificationsRepository`**: A new repository to abstract the fetching of notification data from Firestore.
- **Deep-Linking**: The app's `NavHost` and `Activity` will need to be configured to handle incoming intents with deep-link data to navigate to the correct screen.
