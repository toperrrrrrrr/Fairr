# Improvements Deep Dive: User Profile & Settings

This document outlines the design and implementation for the user profile and account settings screen. This feature allows users to manage their personal information and credentials.

## 1. User Stories

- **As a user**, I want to be able to change my display name so that other group members can identify me easily.
- **As a user**, I want to upload or change my profile picture to personalize my account.
- **As a user**, I want to be able to change my password to keep my account secure.
- **As a user**, I want to be able to delete my account and all associated data.

## 2. UI/UX Design

- **Entry Point**: There will be a "Profile", "Account", or "Settings" option in the main navigation drawer or a tab bar.
- **Profile Screen**: This screen will display the user's current profile picture, display name, and email address.
- **Editable Fields**: The display name will be an editable text field. Tapping on the profile picture will open an image picker (gallery or camera).
- **Actions**: The screen will have distinct buttons or menu options for "Change Password" and "Delete Account".
- **Confirmation Dialogs**: All critical actions (changing a password, deleting an account) must trigger a confirmation dialog that clearly explains the consequences.

## 3. Backend & Firebase Implementation

This feature primarily interacts with Firebase Authentication and the `users` collection in Firestore.

### Changing Display Name
1.  The user edits their name in the UI and saves.
2.  The client calls the `updateProfile` function from the Firebase Auth SDK.
3.  Simultaneously, the client updates the `displayName` field in the corresponding `users/{userId}` document in Firestore to ensure consistency.
    - **Note**: We may later move this to a Firebase Function to ensure the Auth and Firestore profiles never go out of sync.

### Changing Profile Picture
1.  The user selects a new image from their device.
2.  The client uploads the image to **Firebase Storage** in a path like `profile_pictures/{userId}/profile.jpg`.
3.  After a successful upload, the client gets the public download URL for the image.
4.  The client updates the `photoUrl` field in both Firebase Auth (using `updateProfile`) and the `users/{userId}` Firestore document.

### Changing Password
1.  The user clicks "Change Password".
2.  The app uses the Firebase Auth SDK's `updatePassword` method.
3.  **Security**: This is a sensitive operation. The Firebase Auth SDK may require the user to have signed in recently. Best practice is to prompt the user to re-enter their current password before allowing a change.

### Deleting Account
1.  This is the most critical and destructive action. It must be handled by a **callable Firebase Function** (`deleteUserAccount`).
2.  The function will perform the following steps:
    1.  Delete the user's record from Firebase Authentication.
    2.  Delete the user's document from the `users` collection in Firestore.
    3.  Delete the user's data from Firebase Storage (e.g., their profile picture).
    4.  **Crucially**: It should also trigger a process to scrub the user's data from all groups they were a part of (e.g., remove them from `memberIds` arrays). This is a complex operation that highlights why it must be done on the backend.

## 4. Security Considerations

- **Re-authentication**: For sensitive actions like changing a password or deleting an account, force the user to re-authenticate by entering their password again.
- **Storage Rules**: Firebase Storage will need security rules to ensure that a user can only write to their own `profile_pictures/{userId}/` directory.
  ```
  match /profile_pictures/{userId}/{fileName} {
    allow read; // Allow public read for profile pictures
    allow write: if request.auth.uid == userId;
  }
  ```

## 5. Frontend Architecture

- **`ProfileViewModel`**: A new ViewModel dedicated to managing the logic for the profile screen.
- **`UserRepository`**: The existing `UserRepository` will be expanded to include methods for updating the user's Firestore document.
- **`StorageRepository`**: A new repository may be created to handle the logic for uploading images to Firebase Storage.
- **UI State**: The `ProfileUiState` will hold the current user information and loading/error states for the various update operations.
