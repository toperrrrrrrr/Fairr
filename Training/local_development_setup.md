# Local Development Setup Guide

This document provides a step-by-step guide for setting up the Fairr project for local development. Following these instructions will ensure you have a consistent and functional development environment.

## 1. Prerequisites

Before you begin, ensure you have the following software installed on your machine:

- **Git**: For version control. [Download Git](https://git-scm.com/downloads)
- **Java Development Kit (JDK)**: Version 11 or higher. [Download OpenJDK](https://openjdk.java.net/)
- **Android Studio**: The latest stable version. [Download Android Studio](https://developer.android.com/studio)
- **Node.js**: Required for the Firebase CLI. Use the latest LTS version. [Download Node.js](https://nodejs.org/)

## 2. Project Setup

1.  **Clone the Repository**: Open your terminal and clone the project repository to your local machine.
    ```bash
    git clone <repository-url>
    cd Fairr
    ```

2.  **Open in Android Studio**: Launch Android Studio and select "Open an Existing Project", then navigate to the cloned `Fairr` directory.

## 3. Firebase Project Setup

To develop locally, you need a Firebase project to connect to. You will configure the app to use the **local Firebase Emulator Suite**, but you still need a real Firebase project to download the necessary configuration file.

1.  **Create a Firebase Project**: Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.

2.  **Add an Android App**:
    - In your new project, click the Android icon to add a new Android app.
    - The **Android package name** can be found in the `app/build.gradle.kts` file (look for `applicationId`). It is likely `com.example.fairr`.
    - Follow the on-screen steps. When prompted, **download the `google-services.json` file**.

3.  **Place the Config File**: Move the downloaded `google-services.json` file into the `app/` directory of the Android project.

4.  **Enable Firebase Services**: In the Firebase Console, go to the "Build" section and enable the following services:
    - **Authentication**: Enable the "Email/Password" and "Google" sign-in providers.
    - **Firestore Database**: Create a new Firestore database. Start in **test mode** for now (we will add security rules later).
    - **Storage**: Enable Cloud Storage.
    - **Functions**: Enable Cloud Functions.

## 4. Firebase Emulator Suite Setup

The Firebase Emulator Suite allows you to run emulated versions of Firebase services on your local machine. This is faster, safer, and free.

1.  **Install the Firebase CLI**: Open your terminal and install the Firebase Command Line Interface globally using npm.
    ```bash
    npm install -g firebase-tools
    ```

2.  **Login to Firebase**: Log in with the Google account you used to create the Firebase project.
    ```bash
    firebase login
    ```

3.  **Initialize Firebase in the Project**: In the root directory of the cloned project, initialize Firebase.
    ```bash
    firebase init
    ```
    - Select **"Use an existing project"** and choose the project you created.
    - When prompted to choose features, use the spacebar to select:
        - `Firestore`
        - `Functions`
        - `Storage`
        - `Emulators`
    - Follow the prompts, accepting the defaults for file names (`firestore.rules`, `storage.rules`, etc.).
    - For Functions, select **TypeScript** as the language.
    - For Emulators, select `Authentication`, `Firestore`, `Functions`, and `Storage`. Download them when prompted.

4.  **Start the Emulators**: Once initialized, start the emulator suite from the project root.
    ```bash
    firebase emulators:start
    ```
    - This will start all the emulators and provide you with a local URL for the Emulator UI (usually `http://localhost:4000`).

## 5. Running the Android App

Finally, you need to configure the Android app to connect to the local emulators instead of the live Firebase services.

1.  **Configure Emulator Connection**: In the app's code (likely in an `AppModule` or a similar Hilt/Dagger module), you will need to add logic to point the Firebase SDKs to the local emulators when the app is in a debug build. For example:
    ```kotlin
    // Example for Firestore
    if (BuildConfig.DEBUG) {
        firestore.useEmulator("10.0.2.2", 8080)
    }
    // Note: "10.0.2.2" is the special IP address for the host machine's localhost from the Android emulator.
    ```

2.  **Build and Run**: In Android Studio, select an emulator or a connected physical device and click the "Run" button. The app should now build and launch, connected to your local Firebase Emulator Suite.
