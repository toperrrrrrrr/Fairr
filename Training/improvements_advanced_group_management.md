# Improvements Deep Dive: Advanced Group Management

This document outlines the design and implementation plan for advanced group management features, which are critical for the app's usability. This includes inviting new members, managing pending invitations, and removing existing members.

## 1. User Stories

- **As a group member**, I want to invite a new person to the group by using their email address so that they can join and participate in our shared expenses.
- **As an invited user**, I want to receive a notification or see a pending invitation within the app so that I can choose to accept or decline it.
- **As a group creator/admin**, I want to be able to remove a member from the group in case they are no longer part of our shared activities.
- **As a group creator/admin**, I want to be able to cancel a pending invitation that I have sent.

## 2. UI/UX Design

- **Invite Flow**: In the group details screen, there will be an "Add Member" or "Invite" button. This will open a dialog where a user can enter the email address of the person they wish to invite.
- **Pending Invites UI**: A user who has been invited to a group will see a prominent "Pending Invitations" section on their main dashboard or groups list screen. Each pending invite will show the group name and who invited them, with clear "Accept" and "Decline" buttons.
- **Member List UI**: The list of group members will have a small 'X' or a three-dot menu next to each member's name (except their own), allowing the group admin to initiate the removal process. This action should trigger a confirmation dialog to prevent accidental removals.

## 3. Backend & Firestore Implementation

This feature will be implemented using the new `invites` collection.

### `invites` Collection
- **Path**: `invites/{inviteId}`
- **Purpose**: To securely manage and track the state of all invitations.
- **Schema**: See the `firestore_collections.md` document for the detailed schema.

### The Invite-Accept-Join Flow

1.  **Sending an Invite**: 
    - When a member invites a new user via email, a new document is created in the `invites` collection with a status of `pending`.
    - This will be handled by a **callable Firebase Function** (`sendGroupInvite`) to ensure the inviter is actually a member of the group they're inviting someone to.

2.  **Viewing Pending Invites**:
    - When a logged-in user opens the app, the client will query the `invites` collection for any documents where `inviteeEmail` matches their own email and the `status` is `pending`.

3.  **Accepting an Invite**:
    - When the user clicks "Accept", another **callable Firebase Function** (`acceptGroupInvite`) is triggered.
    - This function will perform the following steps in a single atomic **Firestore Transaction**:
        1.  Verify the invite is still `pending`.
        2.  Add the user's `userId` to the `memberIds` array in the corresponding `groups/{groupId}` document.
        3.  Update the status of the invite document in the `invites` collection to `accepted`.

4.  **Declining an Invite**:
    - If the user clicks "Decline", a function updates the invite document's status to `declined`.

## 4. Security Considerations

- **Firestore Rules for `invites`**: 
  - A user can only create an invite if they are a member of the group they are inviting to.
  - A user can only read an invite if their email matches the `inviteeEmail` field.
  - A user can only update an invite's status (to accept/decline) if their email matches.
- **Server-Side Validation**: All critical actions (sending, accepting, removing) will be handled by Firebase Functions to ensure that a user cannot, for example, add themselves to a group they were not invited to.
- **Removing a Member**: Removing a member will also be a Firebase Function (`removeGroupMember`). This function will ensure the person initiating the removal has the authority to do so (e.g., is a group admin) and will remove the user's ID from the `memberIds` array.

## 5. Frontend Architecture

- **`GroupManagementViewModel`**: A new or existing ViewModel will be responsible for handling the logic for inviting, accepting, declining, and removing members.
- **`InviteRepository`**: A new repository will be created to abstract all interactions with the `invites` collection and the related Firebase Functions.
- **UI State**: The ViewModel's `UiState` will be updated to include a list of pending invitations to be displayed by the UI.
