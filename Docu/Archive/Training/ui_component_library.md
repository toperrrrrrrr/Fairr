# UI Component Library

This document provides an overview of the reusable UI components available in the Fairr application. These components are built with Jetpack Compose and are designed to be modular, customizable, and consistent with the app's modern design language.

## Core Principles

- **Reusability**: Components are designed to be used across multiple screens with minimal configuration.
- **Customizability**: Components expose parameters to allow for flexibility in appearance and behavior.
- **Consistency**: All components adhere to the app's theming and design guidelines, ensuring a cohesive user experience.

## Component Files

This library is organized into several key files within the `ui/components` directory:

### `Cards.kt`

Contains card-based components for displaying summary information.

- **`OverviewCard`**: A card for displaying a user's total balance and providing navigation to budget details.
- **`GroupCard`**: A card for displaying summary information about a group, including its name, number of members, and the user's balance within the group.

### `ImageComponents.kt`

Provides a set of standardized components for loading and displaying images.

- **`FairrImage`**: A generic, reusable image loading component that handles loading and error states.
- **`ProfileImage`**: A circular image component specifically for displaying user profile pictures.
- **`ReceiptImage`**: A component for displaying receipt images, with support for loading indicators.

### `LoadingSpinner.kt`

- **`LoadingSpinner`**: A simple, centered circular progress indicator for use during data loading.

### `ModernComponents.kt`

This file contains a rich set of modern, reusable components that form the core of the app's design system.

- **`ModernCard`**: A highly customizable base card component with shadow and corner radius options.
- **`ModernItemCard`**: A specialized card for displaying items in a list, such as expenses or group members.
- **`ModernProgressBar`**: A sleek, animated progress bar.
- **`ModernBadge`**: A small badge for displaying status information (e.g., "New").
- **`ModernButton`**: An advanced button component with support for icons.
- **`ModernStatsCard`**: A card for displaying key statistics with a title, value, and an optional change indicator.
- **`ModernTextField`**: A feature-rich text field with support for leading/trailing icons, password visibility toggling, and error messages.
- **`ModernHeader`**: A header component for screen titles and subtitles.
- **`ModernListItem`**: A flexible list item component with support for leading icons and trailing content.
- **`ModernSectionHeader`**: A header for sections within a screen, with support for a title, subtitle, and an optional action.
- **`ModernDivider`**: A subtle divider for separating content.

### `CommonComponents.kt`

This file contains a collection of general-purpose UI components that are used throughout the application.

- **`PrimaryButton`**: A standard button with the app's primary color scheme, used for the main call-to-action on a screen.
- **`SecondaryButton`**: A button with a secondary color scheme, used for less prominent actions.
- **`InputField`**: A styled text field for user input, complete with placeholder text and error handling.
- **`Header`**: A composable for displaying a screen title and optional back navigation.

### `ModernUXComponents.kt`

This file contains more complex, modern UI components that contribute to a polished user experience.

- **`InfoChip`**: A small, dismissible chip for displaying brief, contextual information.
- **`ConfirmationDialog`**: A standardized dialog for confirming user actions (e.g., deleting an item).
- **`ModernBottomNavBar`**: The app's primary bottom navigation bar, featuring animated icons and a clean design.
- **`ModernFAB`**: A floating action button with a consistent style, used for primary actions like adding a new expense or group.
