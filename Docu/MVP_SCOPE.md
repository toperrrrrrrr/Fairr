# Fairr MVP Scope & Gap Analysis

_Last updated: <!--date will be inserted by user env-->_

## 1. MVP Definition
The Minimum Viable Product for **Fairr** (a Splitwise-style group-expense tracker) is the smallest, stable set of features that allows a small group of friends to track shared costs and settle up securely.  The MVP must support:

| Domain | Must-Have Capability |
|--------|---------------------|
| **Access & Identity** | • Email/password & Google sign-in  <br>• Verified account persistence across sessions |
| **Group Management** | • Create group with name, currency & description  <br>• Join group via invite code  <br>• Leave group / Remove member  <br>• Delete group (admin only) |
| **Expense Tracking** | • Add expense with description, amount, date, payer  <br>• Equal / % / custom splits  <br>• Edit or delete expense (creator or admin) |
| **Balances & Settlement** | • Real-time per-user balances per group  <br>• Optimised debt list (`SettlementService`)  <br>• Record a settlement payment and mark shares paid |
| **Navigation & UX** | • Bottom-nav dashboard (Home / Groups / Friends / Notifications / Settings)  <br>• Search expenses & groups  <br>• Empty / error states so the app never feels broken |
| **Security & Data** | • Firestore rules enforce auth + membership  <br>• Offline cache (Firestore persistence)  <br>• Analytics/Crashlytics disabled in dev |

## 2. Current Implementation Snapshot
✅ = fully working • 🚧 = partial • ⛔️ = missing

| Area | Status | Notes |
|------|--------|-------|
| Email / Google auth | ✅ | Via `AuthService` / FirebaseUi |
| Create / Join / Leave / Delete group | ✅ | Leave & remove actions recently wired |
| Invite code generation | ✅ | 6-char code on create |
| Add expense (equal split) | ✅ | Works end-to-end |
| Percentage / Custom splits | 🚧 | Logic done in repo; UI picker still stub |
| Edit / Delete expense | 🚧 | `EditExpenseScreen` has TODOs |
| Per-user balances | ✅ | Dashboard & group detail use `SettlementService` |
| Record settlement | ✅ | Persists doc + flags splits paid |
| Search | 🚧 | UI ready; no Firestore backing query |
| Friends (add / accept / list) | ✅ | End-to-end with UI |
| Notifications | 🚧 | Screen exists; service & rules done; push integration TBD |
| Settings (currency, profile pic) | 🚧 | Currency picker wired to DataStore; profile image picker stub |
| Offline cache | ✅ | Firestore persistence enabled |
| Security rules | ✅ | Expenses, settlements, summaries validated |

## 3. MVP Blocking Gaps
1. **Expense editing & deletion** – finish `EditExpenseScreen` mutations & rules.  
2. **Split UI for % / Custom** – surface the new calculation paths in `AddExpenseScreen`.  
3. **Search backend** – implement Firestore composite queries (expenses by description, group by name).  
4. **Basic Push Notifications** – at minimum, local in-app badge for friend/group invites.  
5. **Profile photo picker** – gallery / camera upload to Storage.

## 4. Nice-to-Have Post-MVP
• Export data to CSV/PDF  
• Image OCR for receipt scanning  
• Budgets & category analytics  
• Dark-mode theming + accessibility pass  
• CI pipeline with emulator tests

## 5. Recommended Next Sprint (2 weeks)
1. Finish expense edit/delete (`ExpenseRepository` & UI).  
2. Implement split picker UI.  
3. Firestore search queries + index update.  
4. Wire local notifications list refresh.  
5. Profile image upload.

---
Once these gaps are closed, Fairr reaches a functional MVP suitable for closed-beta release. 