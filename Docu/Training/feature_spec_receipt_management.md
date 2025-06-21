# Feature Spec: Receipt Management

This document provides the technical specification for the Receipt Management feature, which will allow users to upload, view, and manage receipts for their expenses.

## 1. Feature Overview

Users will be able to attach one or more images of receipts to an expense. This will provide a clear record of the purchase and help with expense verification.

## 2. Technical Implementation

### 2.1. File Storage

- **Service**: Firebase Storage will be used to store the receipt images.
- **Folder Structure**: Images will be stored in a dedicated `receipts` folder at the root of the Firebase Storage bucket. To avoid conflicts and improve organization, images will be stored in a path format that includes the group ID and expense ID:
  `receipts/{groupId}/{expenseId}/{imageName}.jpg`

### 2.2. Image Handling

- **Image Source**: Users can either capture a new photo using the device camera or select an existing image from the gallery.
- **Image Compression**: To save storage space and reduce upload times, images will be compressed before being uploaded. A target resolution (e.g., 1920x1080) and quality setting (e.g., 75%) will be applied.
- **File Naming**: Each uploaded image will be given a unique name, such as a timestamp or a UUID, to prevent name collisions.

### 2.3. Database Integration

- **Firestore Linking**: After an image is successfully uploaded to Firebase Storage, its download URL will be stored in the `attachments` array of the corresponding `Expense` document in Firestore. The `Expense` data model already supports this with its `attachments: List<String>` field.

## 3. User Interface / User Experience (UI/UX)

- **Add/Edit Expense Screen**: 
  - A new section will be added to this screen for managing attachments.
  - An "Add Receipt" button will allow users to trigger the image capture/selection flow.
  - Thumbnails of the attached receipts will be displayed, with an option to remove an attachment.
- **Expense Detail Screen**:
  - Attached receipt thumbnails will be displayed.
  - Tapping a thumbnail will open a full-screen image viewer, allowing the user to zoom and pan.

## 4. Security

- **Firebase Storage Rules**: Security rules will be configured to ensure that only authenticated users who are members of the corresponding group can upload or view receipts.

  ```json
  {
    "rules": {
      "receipts": {
        "{$groupId}": {
          "{$expenseId}": {
            // Allow read/write access only to users who are members of the group
            ".read": "resource.metadata.group_id == $groupId && request.auth != null && exists(/databases/(default)/documents/groups/$(groupId)) && request.auth.uid in get(/databases/(default)/documents/groups/$(groupId)).data.members",
            ".write": "resource.metadata.group_id == $groupId && request.auth != null && exists(/databases/(default)/documents/groups/$(groupId)) && request.auth.uid in get(/databases/(default)/documents/groups/$(groupId)).data.members"
          }
        }
      }
    }
  }
  ```
  **Note**: The above security rules are a starting point and may need to be refined based on the exact data structure of group members.
