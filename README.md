﻿# WebRTC-Compose-Video-Calling-App
This is a **WebRTC-based video calling app** built with **Jetpack Compose** in Android Studio. It uses **Firebase Firestore** as a signaling server, **WebRTC** for real-time communication, and follows modern Android development practices.  

## 🚀 Features  

- ✅ Peer-to-peer video calling using WebRTC  
- ✅ Firebase Firestore for signaling  
- ✅ Jetpack Compose UI  
- ✅ Permissions handling with Accompanist  
- ✅ Navigation using Compose Navigation  

---

## 🛠 Getting Started  

### 1️⃣ Create a Jetpack Compose Project  

1. Open **Android Studio** and create a new project.  
2. Select **"Empty Compose Activity"** as the template.  
3. Name your project and finish the setup.  

---

## 🔧 Setup  

### 2️⃣ Modify Gradle Files  

#### 📌 Step 1: Update `settings.gradle.kts` (Project Level)  

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Add JitPack for WebRTC
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Add JitPack for WebRTC
    }
}
```
#### 📌 Step 2: Update `build.gradle.kts` (Project Level)

```kotlin
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("com.android.library") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false // Firebase Plugin
}
```
#### 📌 Step 3: Update build.gradle.kts (Module Level - app)
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // Firebase Plugin
}
```
### 📦 3️⃣ Add Dependencies
#### Open your build.gradle.kts (Module: app) file and add the following dependencies:

```kotlin
dependencies {
    // Logging
    implementation "com.jakewharton.timber:timber:5.0.1"

    // Permission Handling
    implementation "com.google.accompanist:accompanist-permissions:0.30.1"

    // WebRTC
    implementation "org.webrtc:google-webrtc:1.0.32006"

    // Compose Navigation
    implementation "androidx.navigation:navigation-compose:2.7.2"

    // Firebase Firestore for signaling server
    implementation "com.google.firebase:firebase-firestore-ktx:24.7.1"

}
```
## 💡 Note: After adding these dependencies, click "Sync Now" in Android Studio.

### 🔥 4️⃣ Set Up Firebase

#### 📌 Step 1: Create a Firebase Project
1. Navigate to the [Firebase Console](https://console.firebase.google.com/).
2. Click **"Create a project"** and provide a project name.
3. Follow the on-screen instructions and enable Google Analytics (optional).
4. Once done, click **"Continue"** and wait for Firebase to complete the project setup.


#### 📌 Step 2: Add Firebase to Your Android App
1. In the Firebase Console, go to **Project settings > General**.
2. Click **"Add app"** and select **Android**.
3. Enter your app's **package name** (ensure it matches the one in your `AndroidManifest.xml`).
4. Click **"Register App"** and then download the `google-services.json` file.
5. Place the `google-services.json` file inside the following directory:
#### 📌 Step 3: Enable Firestore
1. In the Firebase Console, navigate to **Firestore Database**.
2. Click **"Create database"**, and choose **Start in test mode** (recommended for development).
3. Click **"Next"**, select your preferred database location, and enable Firestore.

### ✅ **Done!** Firebase is now set up for your WebRTC signaling server.
