# Improvements Deep Dive: App-Level Settings

This document outlines the design and implementation of a centralized App Settings screen, allowing users to customize their application-wide experience.

## 1. User Stories

- **As a user**, I want a single place to manage all my app settings so I don't have to hunt for them.
- **As a user**, I want to be able to turn different types of push notifications on or off to control how often the app contacts me.
- **As a user**, I want to be able to switch between a light and dark theme to suit my preference and environment.
- **As a user**, I want to set a default currency that will be pre-selected when I create a new group.

## 2. UI/UX Design

- **Entry Point**: A "Settings" option will be available in the main navigation drawer or from the User Profile screen.
- **Settings Screen**: The screen will be organized into logical sections (e.g., "Notifications", "Appearance", "General").
- **Controls**: Standard UI controls will be used for each setting:
  - **Notification Toggles**: A master switch to enable/disable all notifications, followed by individual switches for each type (e.g., "New Expenses", "Settlements", "Group Invites").
  - **Theme Selector**: A segmented control or radio button group allowing the user to select "Light", "Dark", or "System Default".
  - **Default Currency**: A dropdown menu or a clickable row that opens a currency selection screen.

## 3. Technical Implementation

User settings are specific to the device and user session, so they will be stored locally. We will use **Jetpack DataStore** for this purpose, as it is the modern, recommended solution for storing simple key-value data asynchronously.

### Jetpack DataStore Setup
1.  **Preferences DataStore**: We will create a `Preferences DataStore` to store the user's settings.
2.  **Settings Keys**: We will define typed keys for each setting (e.g., `booleanPreferencesKey("notifications_enabled")`, `stringPreferencesKey("app_theme")`).

### `SettingsRepository`
- A new `SettingsRepository` will be created to abstract all interactions with the DataStore.
- It will expose a `Flow<UserSettings>` that the rest of the app can observe to react to changes in settings in real-time.
- It will also provide `suspend` functions to update each setting (e.g., `updateTheme(theme: String)`).

### Integrating Settings with Features
- **Notifications**: The Firebase Function responsible for sending notifications will first check a user's notification preferences (which could be synced to their Firestore `users` document) before sending a push notification.
- **Theming**: The root `Composable` of the application will observe the theme setting from the `SettingsRepository` and apply the chosen theme (light/dark) to the entire app.
- **Default Currency**: When the user navigates to the "Create Group" screen, the ViewModel will fetch the default currency from the `SettingsRepository` and pre-populate the currency field.

## 4. Frontend Architecture

- **`SettingsViewModel`**: A new ViewModel to manage the logic for the settings screen. It will read the current settings from the `SettingsRepository` and expose them to the UI, and it will call the repository's update methods when the user changes a setting.
- **`SettingsUiState`**: A data class representing the state of all settings to be displayed on the screen.
- **Dependency Injection**: The `SettingsRepository` and the DataStore instance will be provided via Hilt to the ViewModels that need them.
