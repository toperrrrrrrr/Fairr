# Fairr Android App - TODO List (Organized by Implementation Time)

## 🚀 QUICK FIXES (1-4 hours each)

### Currency & Display Issues
- [x] **Fix currency symbol display consistency**
  - Update `CurrencyFormatter.kt` to use dynamic currency from user settings
  - Remove hardcoded USD references in ViewModels
  - Files: `util/CurrencyFormatter.kt`, `data/settings/SettingsDataStore.kt`

- [ ] **Replace main page settings button**
  - Replace settings button with profile avatar or hamburger menu
  - Files: `ui/screens/home/HomeScreen.kt`, `ui/screens/MainScreen.kt`

### Navigation & UI Polish
- [ ] **Reduce navigation bar white space**
  - Modify padding/margins in `ModernNavigationBar.kt`
  - Implement oblong floating button shapes
  - Files: `ui/components/ModernNavigationBar.kt`

- [ ] **Add keyboard auto-hide functionality**
  - Implement keyboard hiding when calculator is clicked
  - Add tap-to-dismiss behavior for text fields
  - Files: `ui/components/Calculator.kt`, text input screens
  - Add expense page.

### Validation & Error Handling
- [ ] **Improve email validation**
  - Add robust email validation in friend invitations
  - Implement proper error messaging for invalid formats
  - Files: `ui/screens/friends/FriendsViewModel.kt`, `data/friends/FriendService.kt`

## ⚡ MEDIUM FIXES (4-8 hours each)

### Authentication & Session Management
- [ ] **Fix session persistence issues**
  - Enhance `AuthService.kt` for proper session validation
  - Modify `StartupViewModel.kt` to check Firebase Auth state
  - Update `FairrNavGraph.kt` for conditional auth screens
  - Files: `data/auth/AuthService.kt`, `ui/viewmodels/StartupViewModel.kt`, `navigation/FairrNavGraph.kt`

- [ ] **Implement proper sign-out data clearing**
  - Clear `UserPreferencesManager` and `SettingsDataStore` on logout
  - Reset all ViewModels to initial state
  - Force account selection dialog on next login
  - Files: `data/auth/AuthService.kt`, `data/preferences/UserPreferencesManager.kt`

### Group Management Features
- [ ] **Complete remove member functionality**
  - Implement `removeMemberFromGroup()` in `GroupSettingsViewModel.kt`
  - Add confirmation dialog and notifications
  - Files: `ui/viewmodel/GroupSettingsViewModel.kt`, `data/groups/GroupService.kt`

- [ ] **Implement edit group functionality**
  - Add group editing logic in `GroupSettingsViewModel.kt`
  - Create update methods in `GroupService.kt`
  - Files: `ui/viewmodel/GroupSettingsViewModel.kt`, `data/groups/GroupService.kt`

### UI Component Improvements
- [ ] **Redesign notification cards**
  - Use `ModernComponents.kt` patterns for notification UI
  - Add card elevation, visual hierarchy, and action buttons
  - Consider swipe-to-dismiss functionality
  - Files: `ui/screens/notifications/NotificationsScreen.kt`, `ui/components/ModernComponents.kt`

- [ ] **Add emoji support for groups**
  - Create emoji picker component
  - Integrate in group creation and detail screens
  - Files: `ui/screens/groups/CreateGroupScreen.kt`, `ui/screens/groups/GroupDetailScreen.kt`

### Profile & Onboarding
- [ ] **Add profile completion prompts**
  - Implement profile completion checking
  - Add onboarding flow for missing details
  - Create prompts in home screen
  - Files: `ui/viewmodels/ProfileViewModel.kt`, `data/repository/UserRepository.kt`

## 🔧 COMPLEX FIXES (8-16 hours each)

### Real-time Data & Performance
- [ ] **Fix group details real-time updates**
  - Implement Firestore real-time listeners in `GroupDetailViewModel`
  - Ensure proper data refresh after expense operations
  - Use Flow-based data streams for reactive updates
  - Files: `ui/screens/groups/GroupDetailViewModel.kt`, `data/expenses/ExpenseService.kt`

- [ ] **Comprehensive group expense flow audit**
  - Test end-to-end: Group Creation → Expense Logging → Settlement
  - Verify data consistency between ViewModels and Services
  - Create integration tests for each flow step
  - Files: All group-related ViewModels, Services, and Screens

### Advanced UI Features
- [ ] **Implement page transition effects**
  - Add custom transitions in `FairrNavGraph.kt`
  - Use Compose Animation APIs (slideInHorizontally, fadeIn, etc.)
  - Create consistent transition patterns
  - Files: `navigation/FairrNavGraph.kt`

- [ ] **User activity tracking system**
  - Implement analytics tracking in `AuthService.kt`
  - Add action logging throughout critical flows
  - Consider Firebase Analytics integration
  - Create user engagement dashboard
  - Files: `data/auth/AuthService.kt`, major ViewModels

## 🏗️ MAJOR IMPLEMENTATIONS (16+ hours each)

### Feature Completeness
- [ ] **Systematic feature completeness review**
  - Compare implementation against `MVP_SCOPE.md`
  - Create feature matrix of planned vs. implemented
  - Identify missing critical features (OCR, offline support, analytics)
  - Prioritize based on user impact

### Testing & Quality Assurance
- [ ] **Comprehensive testing suite**
  - Unit tests for ViewModel logic
  - Integration tests for Service layer
  - UI tests for critical user flows
  - Performance testing and optimization

### Security & Data Management
- [ ] **Advanced security implementation**
  - Validate all user inputs comprehensively
  - Implement robust Firestore security rules
  - Ensure secure authentication flows throughout
  - Implement proper session management

---

## 📋 IMPLEMENTATION GUIDELINES

### Development Standards
- Follow Clean Architecture + MVVM patterns
- Maintain existing naming conventions
- Use Hilt for dependency injection
- Implement proper error handling
- Use coroutines and Flow for async operations

### Testing Requirements
Each TODO should include:
- [ ] Unit tests written
- [ ] Integration tests (if applicable)
- [ ] Manual testing completed
- [ ] Performance impact assessed

### Priority Indicators
- 🚀 **Quick Fixes**: Can be completed in a single session
- ⚡ **Medium Fixes**: Require focused development time
- 🔧 **Complex Fixes**: Need careful planning and testing
- 🏗️ **Major Implementations**: Significant feature development

---

## 🎯 RECOMMENDED SPRINT PLANNING

### Sprint 1 (Week 1): Quick Wins
Focus on 🚀 Quick Fixes to improve immediate user experience

### Sprint 2 (Week 2): Core Functionality
Address ⚡ Medium Fixes, especially authentication and group management

### Sprint 3 (Week 3): Advanced Features
Tackle 🔧 Complex Fixes for real-time updates and UI enhancements

### Sprint 4 (Week 4): Polish & Testing
Complete 🏗️ Major Implementations and comprehensive testing

---

*Total Estimated Time: 60-80 hours of development work*
*Recommended Team Size: 2-3 developers working in parallel* 