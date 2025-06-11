# Firebase Functions API Reference

This document serves as the official API contract for all callable Firebase Cloud Functions used by the Fairr application. It defines the expected request payloads and the possible response structures for both success and error cases.

**Base URL**: All functions are callable via the Firebase Functions SDK.
**Authentication**: All functions require the user to be authenticated. The user's `uid` will be available in the `context.auth` object on the backend.

---

## 1. Debt & Settlement Functions

### `calculateDebtSimplification`

Calculates the minimum number of transactions required to settle all debts within a group.

- **Request Payload**:
  ```json
  {
    "groupId": "string"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "transactions": [
      {
        "fromUserId": "string",
        "fromUserName": "string",
        "toUserId": "string",
        "toUserName": "string",
        "amount": "number"
      }
    ]
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: If `groupId` is missing or invalid.
  - `401 Unauthorized`: If the user is not authenticated.
  - `403 Forbidden`: If the user is not a member of the specified group.
  - `404 Not Found`: If the group does not exist.

---

## 2. Group Management Functions

### `sendGroupInvite`

Creates an invitation for a user to join a group.

- **Request Payload**:
  ```json
  {
    "groupId": "string",
    "inviteeEmail": "string"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "status": "success",
    "message": "Invitation sent successfully."
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: If payload is malformed or email is invalid.
  - `403 Forbidden`: If the inviter is not a member of the group.
  - `409 Conflict`: If the user is already a member or has a pending invite.

### `acceptGroupInvite`

Allows a user to accept a pending group invitation.

- **Request Payload**:
  ```json
  {
    "inviteId": "string"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "status": "success",
    "message": "Successfully joined group."
  }
  ```
- **Error Responses**:
  - `403 Forbidden`: If the authenticated user does not match the invitee.
  - `404 Not Found`: If the invite does not exist or has expired.

### `removeGroupMember`

Removes a member from a group. (Requires admin privileges).

- **Request Payload**:
  ```json
  {
    "groupId": "string",
    "memberIdToRemove": "string"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "status": "success",
    "message": "Member removed successfully."
  }
  ```
- **Error Responses**:
  - `403 Forbidden`: If the requester is not an admin of the group.
  - `404 Not Found`: If the group or member does not exist.

---

## 3. User Account Functions

### `deleteUserAccount`

Permanently deletes a user's account and all associated data.

- **Request Payload**: (No payload required, uses the caller's authentication context).
  ```json
  {}
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "status": "success",
    "message": "Account and all associated data have been deleted."
  }
  ```
- **Error Responses**:
  - `401 Unauthorized`: If the user is not authenticated.
  - `500 Internal Server Error`: If the deletion process fails at any step.
