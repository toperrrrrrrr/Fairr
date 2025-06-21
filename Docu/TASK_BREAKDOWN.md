# Fairr Detailed Task Breakdown

_This document explodes the MVP Gap list into concrete, traceable tasks._

## 1. Expense Editing & Deletion
1.1 **Repository Layer**  
1.1.1 Add `updateExpense(expense: Expense)` in `ExpenseRepository` + impl.  
1.1.2 Add `deleteExpense(expenseId: String)` in `ExpenseRepository`.  
1.1.3 Use Firestore `FieldValue.arrayUnion/Remove` for attachment diffs.  
1.1.4 Wrap calls with injected `ioDispatcher` + unit tests.  
1.1.5 Update Firestore composite indexes if needed.

1.2 **Firestore Rules**  
1.2.1 Allow `update` when `request.auth.uid == resource.data.createdBy` **OR** user is group admin.  
1.2.2 Allow `delete` only for admin or creator.  
1.2.3 Add validation for immutable fields (`groupId`, `createdBy`).

1.3 **UI Screen** (`EditExpenseScreen`)  
1.3.1 Pre-fill fields via `Expense` param.  
1.3.2 Hook save button → `viewModel.updateExpense()`.  
1.3.3 Hook delete icon → confirm dialog → `viewModel.deleteExpense()`.  
1.3.4 Show loading & snackbars on success/fail.  
1.3.5 Navigate back and refresh GroupDetail on success.

1.4 **ViewModel**  
1.4.1 Create `EditExpenseViewModel` using single `uiState`.  
1.4.2 Inject `ExpenseRepository` + `GroupService` for validation.  
1.4.3 Expose events (`ExpenseUpdated`, `ExpenseDeleted`, `ShowError`).

---

## 2. Split Picker UI (Percentage & Custom)
2.1 **Design**  
2.1.1 Compose modal sheet with dynamic member list.  
2.1.2 Validate that sum(percentage)==100; show inline errors.

2.2 **AddExpenseScreen Integration**  
2.2.1 Replace chip that toggles split type with detailed sheet.  
2.2.2 Pass selected percentages/custom amounts to `AddExpenseViewModel` (modify data class).

2.3 **Repository Update**  
2.3.1 Accept optional `Map<userId, pct>` &/or `Map<userId, amount>` params; merge into member maps.  
2.3.2 Unit-test edge cases (rounding; missing members).

---

## 3. Search Backend - This can be skipped for now.
3.1 **Query Design**  
3.1.1 Expenses: create composite index `(groupId, description_lowercase)`.  
3.1.2 Groups: create index on `name_lowercase`.

3.2 **Repository**  
3.2.1 Add `searchExpenses(query: String)` & `searchGroups(query: String)` with `startAt()/endAt()` prefix search.  
3.2.2 Return Flow for live updates.

3.3 **SearchScreen Hook-up**  
3.3.1 Replace `getSearchResults()` stub with repository calls.  
3.3.2 Debounce input (300 ms).  
3.3.3 Display "no results" state.

---

## 4. Local Notifications
4.1 **Data Model**  
4.1.1 Finalise `notifications` schema (`type`, `payload`, `read`).

4.2 **Service**  
4.2.1 Poll/subscribe to user's notif collection.  
4.2.2 Transform into UI models & badge counts.

4.3 **UI**  
4.3.1 Update `NotificationsScreen` list & swipe-to-read.

---

## 5. Profile Photo Picker
5.1 **Permissions**  
5.1.1 Add `CAMERA`/`READ_EXTERNAL_STORAGE` to manifest + runtime flow.

5.2 **Image Capture**  
5.2.1 Use `ActivityResultContracts.TakePicturePreview()` for camera.  
5.2.2 Use `ActivityResultContracts.GetContent()` for gallery.

5.3 **Upload**  
5.3.1 Compress & upload to Firebase Storage `/profile_photos/{uid}.jpg`.  
5.3.2 Save URL to `users/{uid}/photoUrl`.

5.4 **EditProfileScreen**  
5.4.1 Replace fab stub with picker logic.  
5.4.2 Show loading indicator & error states.

---

## 6. TODO Annotation Sweep
6.1 Run `grep -R "TODO:"` and tag each with `[MVP]`, `[POST]`, or remove if obsolete.  
6.2 Add ticket numbers once issue tracker is live.

---

## 7. CI / Code Quality (post-MVP but quick wins)
7.1 Configure `ktlint` & `detekt` gradle tasks.  
7.2 GitHub Actions workflow: build-debug, unit tests, lint, detekt.  
7.3 Cache gradle & emulator snapshot for speed.

---

*This document should be updated after every sprint planning session.* 