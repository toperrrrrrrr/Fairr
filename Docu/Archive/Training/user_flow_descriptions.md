# User Flow Descriptions

This document describes the key user flows within the Fairr application, from initial onboarding to core actions like adding expenses and settling balances.

## 1. Onboarding and Authentication

**Goal**: A new user signs up and logs into the app for the first time.

1.  **Splash & Onboarding**: The user launches the app and is greeted by a splash screen, followed by a brief onboarding flow that highlights the app's main features.
2.  **Authentication Options**: The user is presented with options to sign up or log in using:
    - Email and Password
    - Google Sign-in
3.  **Registration**: For a new user, the app collects necessary information (name, email, password) and creates a new user account in Firebase Authentication.
4.  **Login**: An existing user logs in. The app supports biometric authentication (fingerprint/face ID) for subsequent logins if enabled by the user.
5.  **Navigation to Home**: Upon successful authentication, the user is navigated to the main screen of the app (the "Home" tab).

## 2. Creating a New Group

**Goal**: A user creates a new group to share expenses with others.

1.  **Navigate to Groups**: The user selects the "Groups" tab from the bottom navigation bar.
2.  **Initiate Creation**: The user taps a "Create Group" button.
3.  **Enter Group Details**: A new screen appears where the user provides the group's name, an optional description, and sets the default currency.
4.  **Confirm Creation**: The user confirms the details. The app creates a new group document in Firestore and adds the current user as the first member.
5.  **Invite Members**: The user is taken to the newly created group's detail screen, where they can invite other users to join the group.

## 3. Adding a New Expense

**Goal**: A user adds a shared expense to a group.

1.  **Select Group**: The user navigates to the detail screen of the group where the expense occurred.
2.  **Open Add Expense Screen**: The user taps the centered Floating Action Button (FAB) to open the "Add Expense" screen.
3.  **Enter Expense Details**: The user inputs:
    - A description of the expense.
    - The total amount.
    - The member who paid for the expense (`paidBy`).
4.  **Choose Split Method**: The user selects how to split the bill:
    - **Equal**: The cost is divided equally among all members.
    - **Percentage**: The cost is split based on specified percentages.
    - **Custom**: The user manually enters the amount each member owes.
5.  **Confirm Expense**: The user saves the expense. A new expense document is created in Firestore, and the change is reflected in real-time for all group members.

## 4. Settling Balances

**Goal**: A user records a payment to settle a debt with another group member.

1.  **View Balances**: The user navigates to the "Balance" or "Home" screen, where they can see a summary of who owes whom.
2.  **Select Debt**: The app provides simplified debt resolution suggestions (e.g., "You owe Jane $15"). The user selects a debt to settle.
3.  **Record Payment**: The user taps a "Settle Up" button, which brings up a confirmation dialog to record the payment. This action does not involve a real money transfer but updates the internal balances.
4.  **Update Balances**: Once confirmed, the app updates the balances. The transaction is recorded in the group's history, and all members see the updated balances.
