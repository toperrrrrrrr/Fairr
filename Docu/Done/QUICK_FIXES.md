# Quick Fixes for Fairr

A concise, high-impact checklist of tasks that can be completed quickly (≤ 1 day each) to polish the current codebase, remove low-hanging bugs, and shore up security/performance.

## 1. Security Rules
- [x] Add membership validation to `expenses/{expenseId}` rules (only group members can create/update/delete).
- [x] Create rule blocks for the upcoming `settlements` and `group_summaries` collections.
- [x] Enforce read limits (`limit`) and indexed query patterns for large collections (e.g., `notifications`).

## 2. Data-Layer Fixes
- [x] Denormalise `paidByName` when writing an expense to eliminate the **N+1** lookup in `ExpenseRepositoryImpl`.
- [x] Replace hard-coded currency ("USD") with the group's `currency` field when writing and reading expenses.
- [x] Inject `CoroutineDispatcher` (e.g., `@IoDispatcher`) into repositories instead of hard-coding `Dispatchers.IO`.

## 3. Feature Completeness
- [x] Implement percentage & custom splits in `ExpenseRepository.calculateSplits()`.
- [x] Compute `currentUserBalance` in `GroupDetailViewModel` using `SettlementService` results.
- [x] Populate dashboard balance in `GroupListScreen` / dashboard `GroupCard`.
- [x] Wire `SettlementViewModel.recordSettlement()` to create a doc in `settlements` collection and mark splits paid.
- [x] Add *leave group* & *remove member* logic in `GroupSettingsViewModel`.

## 4. UI / UX Polish
- [x] Hook up **Search** navigation from `MainScreen` to `SearchScreen`.
- [x] Use `SettingsDataStore.defaultCurrency` in `AddExpenseViewModel.getCurrencySymbol()` to display the correct symbol.
- [ ] Provide a camera/gallery picker implementation in `EditProfileScreen` (currently stubbed). 
- [x] Display meaningful empty/error states in key screens (Groups, Expenses, Search).

## 5. Testing & CI
- [ ] Add unit tests for `SettlementService` using scenarios in `SettlementCalculationExample.kt`.
- [ ] Add instrumentation test to verify `expenses` security rules deny non-member writes (Firebase emulator).
- [ ] Configure a basic GitHub Actions workflow running `./gradlew lint ktlintCheck detekt`.

## 6. Code Hygiene
- [x] Delete legacy `FairrDestinations.kt` (duplicate of sealed `Screen`).
- [x] Convert remaining multi-mutable-state ViewModels to single `uiState` data-class pattern (FriendsViewModel refactored).
- [ ] Annotate TODO comments with a ticket/owner to avoid lingering placeholders.

## Optional Clean-ups & Follow-ups

These are low-risk tidy-ups discovered while fixing the quick tasks. They can be picked up opportunistically:

- [x] Remove the now-unused `@IoDispatcher` qualifier and `DispatchersModule` duplicates from legacy code tree.
- [x] Ensure UI controls invoke the new `leaveGroup()` / `removeMember()` functions in `GroupSettingsViewModel` (button wiring).