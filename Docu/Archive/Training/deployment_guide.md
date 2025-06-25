# Application Deployment Guide

This document provides a step-by-step checklist for deploying a new version of the Fairr application. Following this guide ensures that all necessary steps are taken for a safe and successful release.

## 1. Pre-Deployment Checklist

Before starting the release process, ensure the following steps have been completed.

- [ ] **Finalize Code**: All feature branches for the release have been merged into the `main` branch.
- [ ] **Run All Tests**: The entire test suite (Unit, Integration, and UI tests) has been run and is passing on the Continuous Integration (CI) server.
- [ ] **Manual QA**: A final round of manual quality assurance has been performed on a release candidate build to catch any issues not covered by automated tests.
- [ ] **Update Version Information**: The version code and version name in the `app/build.gradle.kts` file have been incremented.
  - `versionCode`: An integer that must be incremented for each release.
  - `versionName`: A user-facing string (e.g., "1.1.0").

## 2. Backend Deployment

Backend services must be deployed *before* the client application is released to ensure compatibility.

### Step 2.1: Deploy Firebase Functions

If any Cloud Functions have been added or updated, deploy them using the Firebase CLI from the project root.

```bash
# Deploy only functions
firebase deploy --only functions
```

### Step 2.2: Deploy Firestore Security Rules

If any security rules have been changed, deploy them using the Firebase CLI.

```bash
# Deploy only Firestore rules
firebase deploy --only firestore:rules
```

### Step 2.3: Deploy Storage Security Rules

If any storage security rules have been changed, deploy them using the Firebase CLI.

```bash
# Deploy only Storage rules
firebase deploy --only storage
```

## 3. Android App Release Build

Once the backend is deployed, you can build the signed Android App Bundle (AAB) for release.

1.  **Generate a Signed Bundle**:
    - In Android Studio, go to **Build > Generate Signed Bundle / APK**.
    - Select **Android App Bundle** and click **Next**.
    - Select your key store path, and enter the key store password, key alias, and key password. (This information is sensitive and must be stored securely, not in the repository).
    - Click **Next**.
    - Select the `release` build variant.
    - Click **Finish**.

2.  **Locate the AAB**: The signed AAB file will be located in the `app/release/` directory.

## 4. Google Play Console Release

With the signed AAB, you can now create a new release on the Google Play Console.

1.  **Navigate to your App**: Open the [Google Play Console](https://play.google.com/console) and select the Fairr application.

2.  **Create a New Release**:
    - Go to the **Production** track (or an internal/closed testing track).
    - Click **Create new release**.

3.  **Upload the AAB**: Upload the signed `.aab` file you generated.

4.  **Enter Release Notes**: In the "Release notes" section, add detailed notes about the new features, bug fixes, and improvements in this version.

5.  **Review and Rollout**: Review the release details. If everything is correct, click **Start rollout to production**. You can choose to do a staged rollout (e.g., to 10% of users first) to monitor for any issues before a full release.

## 5. Post-Deployment Monitoring

After the release is live, it is crucial to monitor for any issues.

- **Crashlytics**: Keep a close eye on the Firebase Crashlytics dashboard for any new or unusual crashes.
- **User Feedback**: Monitor user feedback channels (Play Store reviews, support emails) for reports of any problems.
- **Performance Monitoring**: Check Firebase Performance Monitoring for any regressions in app performance.
