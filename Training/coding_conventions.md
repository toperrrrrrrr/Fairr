# Coding Conventions

This document outlines the coding standards, naming conventions, and best practices for the Fairr codebase.

## Language and Frameworks

- **Kotlin**: The primary language for the project. Code should follow the official [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- **Jetpack Compose**: The UI is built with Jetpack Compose. Follow the best practices for state management, component design, and performance as recommended in the official Android documentation.

## Architecture

- **Clean Architecture**: The project adheres to Clean Architecture principles, separating the codebase into three main layers: `data`, `domain`, and `presentation`.
- **MVVM (Model-View-ViewModel)**: The presentation layer uses the MVVM pattern to separate UI logic from business logic.

## Naming Conventions

- **Classes and Interfaces**: Use PascalCase (e.g., `User`, `GroupRepository`).
- **Functions and Variables**: Use camelCase (e.g., `calculateBalance`, `userName`).
- **Constants**: Use UPPER_SNAKE_CASE (e.g., `MAX_GROUP_MEMBERS`).
- **Composable Functions**: Use PascalCase and start with a capital letter (e.g., `UserProfileScreen`, `CustomButton`).
- **ViewModels**: Suffix with `ViewModel` (e.g., `GroupViewModel`).
- **Use Cases**: Name them based on the action they perform (e.g., `GetGroupDetails`, `AddExpense`).

## Best Practices

- **Immutability**: Prefer immutable data structures (`val`, `data class`, `ImmutableList`) wherever possible.
- **Coroutines**: Use Kotlin Coroutines for asynchronous operations. Ensure proper scope management to avoid memory leaks.
- **Dependency Injection**: The project uses Hilt for dependency injection. ViewModels, Repositories, and other classes should be injected using Hilt's annotations (`@HiltViewModel`, `@Inject`, `@Module`, `@Provides`).
- **Error Handling**: Use Kotlin's `Result` class or sealed classes to handle errors gracefully.
- **Testing**: Write unit tests for ViewModels and Use Cases. UI tests should be written for critical user flows.
