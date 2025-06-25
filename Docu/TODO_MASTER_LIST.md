# Fairr Project Master TODO List

This document tracks all major and minor tasks needed to bring the Fairr app to a polished, production-ready state. It is grouped by area and includes both feature work and technical/process improvements. Review and update this list at the start of each sprint.

---

## 🎯 LOW-HANGING FRUIT - START HERE

These tasks are **quick wins** that can be completed in 1-4 hours each and provide immediate value:

### Quick UI Fixes (1-2 hours each)
- [x] **Fix split type display in expense detail** - Currently shows hardcoded "Equal Split" in `ExpenseDetailScreen.kt` line 285
- [x] **Add loading states to expense detail screen** - Show spinner while loading expense data
- [x] **Add error handling to expense detail screen** - Show user-friendly error messages
- [x] **Add confirmation dialog for expense deletion** - Wire up the existing dialog in `EditExpenseScreen.kt`

### Backend Improvements (2-4 hours each)
- [x] **Complete expense editing functionality** - Replace placeholder logic in `EditExpenseScreen.kt` with real data operations
- [x] **Add expense deletion with proper cleanup** - Ensure group totals are updated when expenses are deleted
- [x] **Validate custom splits before saving** - Add client-side validation in `AddExpenseScreen.kt`

### Data Model Fixes (1-2 hours each)
- [x] **Store split type in Firestore** - Currently not persisted, causing the hardcoded display issue
- [x] **Add proper error handling for split calculations** - Handle edge cases in `calculateSplits` method

### Testing & Quality (1-2 hours each)
- [ ] **Add unit tests for split calculation logic** - Test the `calculateSplits` method in `ExpenseRepository.kt`
- [ ] **Test expense CRUD operations** - Verify create, read, update, delete work correctly

---

## 🎯 NEXT QUICK WINS - UPDATED

These tasks are **ready to tackle next** based on our completed work:

### UI/UX Quick Wins (1-3 hours each)
- [x] **Add visual feedback for split type selection** - Enhance the split selection modal with better visual indicators
- [x] **Improve expense detail screen layout** - Better spacing, typography, and visual hierarchy
- [x] **Add empty states to group and expense lists** - Show helpful messages when no data exists

### Backend Quick Wins (2-4 hours each)
- [x] **Add unit tests for split calculation logic** - Test the `calculateSplits` method we just enhanced
- [ ] **Test expense CRUD operations** - Verify create, read, update, delete work correctly
- [ ] **Add group settings edit fields** - Simple text fields for name, description in group settings
- [ ] **Implement basic group deletion** - Add delete button with confirmation dialog

### Data & Security Quick Wins (1-2 hours each)
- [ ] **Add input validation to expense forms** - Validate amounts, descriptions, etc.
- [ ] **Review Firestore security rules** - Ensure basic security is in place
- [ ] **Add error logging for split calculations** - Better debugging for edge cases

---

## 1. Core Feature Completion

### 1.1 Expense Management

#### 1.1.1 Custom Split UI (percentage, shares, custom amounts)
- **UI/UX**
  - [ ] Refine the split selection modal in `AddExpenseScreen.kt` to allow:
    - Interactive percentage input for each member.
    - Custom amount entry per member.
    - Real-time validation: total split must match expense amount (see `calculateSplits` in `ExpenseRepository.kt`).
    - Show remaining/unallocated amount or percentage.
    - Display error if over/under-allocated.
  - [ ] Add visual feedback for split type selection (equal, percentage, custom).
- **ViewModel/Logic**
  - [x] Ensure `AddExpenseViewModel.kt` and `ExpenseRepository.kt` handle all split types robustly.
  - [x] Validate splits before saving (client-side and server-side).
  - [x] Persist split type in Firestore (currently only "Equal Split" is shown in detail screen, see TODO in `ExpenseDetailScreen.kt`).

#### 1.1.2 Validate and persist custom splits
- [x] Ensure backend (`ExpenseRepository.kt`) and Firestore rules enforce:
  - Only valid splits are accepted (sum matches total, no negative values).
  - Split details are stored in the `splitBetween` array.
- [ ] Add unit tests for split calculation logic (see `calculateSplits`).
- [x] Add error handling for invalid splits in UI.

#### 1.1.3 Complete expense editing & deletion (UI + Firestore rules)
- **UI**
  - [x] Replace placeholder logic in `EditExpenseScreen.kt` with real data loading and saving.
  - [ ] Implement split editing in the edit screen (mirroring add flow).
  - [x] Add confirmation dialog for deletion (already present, but wire up to real logic).
- **ViewModel/Repository**
  - [x] Use `EditExpenseViewModel.kt` to fetch, update, and delete expenses via `ExpenseRepository.kt`.
  - [ ] Ensure Firestore rules (`firestore.rules`) only allow group members (or creator/admin) to edit/delete.
  - [x] Update group totals on edit/delete (see transaction logic in `ExpenseRepository.kt`).
- **Testing**
  - [ ] Add tests for update/delete flows, including permission errors.

#### 1.1.4 Recurring expenses (model, UI, scheduling)
- **Model**
  - [ ] Extend `Expense` model to support recurrence (e.g., `recurrenceRule`, `nextOccurrence`).
- **UI**
  - [ ] Add recurrence options to add/edit expense screens (e.g., daily, weekly, monthly).
  - [ ] Show upcoming/active recurring expenses in group and detail screens.
- **Backend**
  - [ ] Implement logic to auto-generate new expenses on schedule (could use Firebase Functions or local scheduling).
  - [ ] Allow users to edit/cancel recurrence.
- **Testing**
  - [ ] Test edge cases (e.g., missed recurrences, group membership changes).

#### 1.1.5 Attachments: Allow users to add receipts/photos to expenses
- **UI**
  - [ ] Add "Add Receipt" button to add/edit expense screens (`AddExpenseScreen.kt`).
  - [ ] Show thumbnails of attached receipts; allow removal before saving.
  - [ ] In `ExpenseDetailScreen.kt`, display attached receipts as thumbnails; tap to view full image.
- **Backend**
  - [ ] Upload images to Firebase Storage (`receipts/{groupId}/{expenseId}/`).
  - [ ] Store download URLs in `attachments` field of `Expense` (already supported in model).
  - [ ] Implement image compression before upload (`PhotoUtils.kt`).
- **Security**
  - [ ] Update Firebase Storage rules to restrict access to group members (see spec in `feature_spec_receipt_management.md`).
- **Testing**
  - [ ] Test upload, view, and delete flows for attachments.

#### 1.1.6 Expense comments/discussion thread
- **Model**
  - [ ] Create a new subcollection (e.g., `expenses/{expenseId}/comments`) in Firestore.
  - [ ] Define `Comment` model (author, timestamp, text).
- **UI**
  - [ ] Add comment thread UI to `ExpenseDetailScreen.kt`.
  - [ ] Allow users to add, edit, and delete their own comments.
- **Backend**
  - [ ] Implement comment CRUD in repository/service.
  - [ ] Add Firestore rules to restrict comment actions to group members.
- **Testing**
  - [ ] Test comment posting, editing, and deletion.

### 1.2 Group Management

#### 1.2.1 Group settings: edit name, currency, description
- [ ] Add edit fields to group settings screen.
- [ ] Validate and persist changes via `GroupService.kt`.
- [ ] Ensure Firestore rules only allow admins to edit group settings.

#### 1.2.2 Group avatar/emoji support
- [ ] Add avatar/emoji picker to group creation and settings screens.
- [ ] Store avatar URL or emoji in group document.
- [ ] Display avatar/emoji in group list, detail, and summary cards.

#### 1.2.3 Group deletion (admin only, with confirmation)
- [ ] Add "Delete Group" button to group settings (visible to admins only).
- [ ] Show confirmation dialog.
- [ ] Use `GroupService.kt` to delete group and all related expenses (see batch logic).
- [ ] Ensure Firestore rules only allow admins to delete.

#### 1.2.4 Role management (admin/member)
- [ ] UI for viewing and changing member roles (admin/member).
- [ ] Backend logic to update roles in group document.
- [ ] Firestore rules to enforce role-based permissions (edit, delete, invite, etc.).

#### 1.2.5 Group invitations and join requests
- [ ] UI for sending invites (by code, link, or email).
- [ ] UI for viewing and accepting/rejecting join requests.
- [ ] Backend logic for managing invites and requests.
- [ ] Firestore rules to restrict who can invite and join.

#### 1.2.6 Group activity feed (recent changes, joins, leaves)
- [ ] Create activity feed model (action, user, timestamp, details).
- [ ] UI to display activity feed in group detail screen.
- [ ] Backend logic to log group events (expense added, member joined/left, etc.).

#### 1.2.7 Group archiving (for inactive groups)
- [ ] Add "Archive Group" action for admins.
- [ ] Update group status in Firestore.
- [ ] Hide archived groups from main list, but allow viewing in archive section.

### 1.3 Settlement & Balances

#### 1.3.1 Advanced settlement algorithms (minimize transactions)
- [ ] Implement debt minimization algorithm in `SettlementService.kt`.
- [ ] Add tests for various group scenarios.
- [ ] UI to show optimized settlement suggestions.

#### 1.3.2 UI for "Settle Up" suggestions and payment tracking
- [ ] Add "Settle Up" button to group and settlement screens.
- [ ] Show suggested payments (who pays whom, how much).
- [ ] Allow users to mark payments as completed.
- [ ] Persist payment records in a `settlements` collection.

#### 1.3.3 Multi-currency support (conversion, display, storage)
- [ ] Extend group and expense models to support different currencies.
- [ ] UI for selecting currency per group and per expense.
- [ ] Integrate currency conversion rates (API or static).
- [ ] Display converted amounts in user's preferred currency.

#### 1.3.4 Manual settlement entry (record cash payments)
- [ ] UI to allow users to record manual payments (e.g., cash).
- [ ] Persist manual settlements in `settlements` collection.
- [ ] Update balances accordingly.

### 1.4 Friends & Social

#### 1.4.1 Friend activity feed
- [ ] Model and UI for showing friend-related activities (added, removed, settled up, etc.).
- [ ] Backend logic to log and fetch friend activities.

#### 1.4.2 Friend suggestions (contacts, mutual groups)
- [ ] UI for suggesting friends based on contacts and mutual groups.
- [ ] Backend logic to fetch and rank suggestions.

#### 1.4.3 Block/report users
- [ ] UI to block or report users from friend or group screens.
- [ ] Backend logic to persist block/report actions.
- [ ] Firestore rules to enforce blocks (e.g., prevent invites, messages).

#### 1.4.4 Friend groups/categories
- [ ] UI to organize friends into categories/groups.
- [ ] Backend support for friend categories.

### 1.5 Notifications

#### 1.5.1 Push notifications (Firebase Cloud Messaging)
- [ ] Integrate FCM for push notifications.
- [ ] Backend logic to send notifications for key events (expense added, invite, comment, etc.).
- [ ] Test on real devices.

#### 1.5.2 In-app notification center polish (swipe, actions)
- [ ] Improve notification center UI (swipe to dismiss, quick actions).
- [ ] Add notification grouping and filtering.

#### 1.5.3 Notification preferences in settings
- [ ] UI for users to set notification preferences (push, email, in-app).
- [ ] Persist preferences in user profile/settings.
- [ ] Respect preferences when sending notifications.

#### 1.5.4 Local reminders for unsettled expenses
- [ ] UI to set reminders for unsettled expenses.
- [ ] Use local notifications/alarms to remind users.

---

## 2. User Experience & UI Polish

- [ ] Audit all screens for Material 3 and Compose consistency
- [ ] Standardize padding, font sizes, color usage
- [ ] Ensure all screens support dark mode
- [ ] Add accessibility labels, test with screen readers
- [ ] Add page transitions and list animations
- [ ] Friendly empty/error states everywhere
- [ ] Add onboarding tour for new users
- [ ] Add loading indicators for all network operations
- [ ] Add undo/redo for destructive actions (e.g., expense delete)

---

## 3. Data & Security

- [ ] Review and harden Firestore security rules (groups, expenses, settlements, users)
- [ ] Add automated tests for security rules
- [ ] Validate all user input (client and server)
- [ ] Sanitize all text fields
- [ ] Ensure Firestore offline persistence works for all flows
- [ ] Add UI indicators for offline/online state
- [ ] Data migration/versioning strategy for future schema changes
- [ ] Audit for potential data leaks (logs, crash reports)

---

## 4. Analytics, Reporting & Export

- [ ] Expense analytics dashboard (charts by category, member, time)
- [ ] Export data to CSV/PDF
- [ ] Budgeting and spending insights
- [ ] Monthly/yearly reports
- [ ] Group and personal finance insights

---

## 5. Profile & Settings

- [ ] Profile photo picker (gallery/camera, cropping)
- [ ] Advanced profile settings (privacy, notification preferences, language, currency)
- [ ] Account deletion (with confirmation and data wipe)
- [ ] Privacy controls (who can see what)
- [ ] User status (active, away, etc.)

---

## 6. Testing & Quality Assurance

- [ ] Unit tests for all ViewModels and repositories
- [ ] Integration tests for end-to-end flows
- [ ] UI tests for critical screens (Compose)
- [ ] Performance testing (slow screens, memory leaks)
- [ ] Test offline/online transitions
- [ ] Test error handling and edge cases
- [ ] Test on multiple device sizes and Android versions
- [ ] Add test coverage reporting to CI

---

## 7. Infrastructure & DevOps

- [ ] CI/CD pipeline (GitHub Actions or similar)
- [ ] Automated linting and static analysis
- [ ] Automated test runs on PRs
- [ ] Play Store beta release prep (icons, screenshots, privacy policy)
- [ ] Crash reporting (Firebase Crashlytics)
- [ ] Analytics (Firebase Analytics, custom events)
- [ ] Automated dependency updates
- [ ] Backup and restore strategy for user data

---

## 8. Documentation

- [ ] Keep `MVP_SCOPE.md` and `