# WebRTC-Compose-Video-Calling-App
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

# WebRTC and Signaling Server for Video Calling App

## Overview
This project implements a video calling feature using **WebRTC** for peer-to-peer communication and **Firebase Firestore** as the signaling server. The signaling server is responsible for exchanging necessary connection details between peers to establish a WebRTC connection.

## WebRTC Explanation
WebRTC (Web Real-Time Communication) enables direct peer-to-peer video, audio, and data communication between users without needing a dedicated media server.

### WebRTC Key Components:
- **PeerConnectionFactory**: Creates WebRTC connections and manages media streams.
- **PeerConnection**: Establishes and maintains the connection between two peers.
- **ICE Candidates**: Help determine the best path for data transfer between peers.
- **SDP (Session Description Protocol)**: Defines media session details like codecs, IP addresses, and ports.

## Role of the Signaling Server
The signaling server facilitates the WebRTC connection by exchanging the following data:
- **SDP Offers & Answers**: Used to negotiate the media connection.
- **ICE Candidates**: Help establish a direct connection between peers.

Firebase Firestore acts as the signaling server, relaying messages between peers.

---

## Project Structure

### 1. **Room Management**
#### `checkRoomCapacity()`
- Checks if a video call room has available slots.
- Redirects users if the room is full.

### 2. **WebRTC Initialization**
#### `initializeWebRTC()`
- Initializes WebRTC by setting up `PeerConnectionFactory` for media processing.

### 3. **Signaling Messages**
#### `sendSignallingMessage()`
- Sends ICE candidates and SDP offers/answers to Firestore.

### 4. **SDP Handling**
#### `sdpObserver`
- Handles the creation and setting of SDP (Session Description Protocol) information.
  - `onCreateSuccess()`: Sets the local SDP and sends it to the server.
  - `onSetSuccess()`: Confirms that SDP has been set and triggers the next step.
  - `onCreateFailure()` and `onSetFailure()`: Handle errors in the SDP process.

#### `createAnswer()`
- Creates an SDP answer when receiving an offer.

### 5. **Handling Incoming Signaling Messages**
#### `handleSignallingMessages()`
- Processes received SDP offers, SDP answers, and ICE candidates.

### 6. **Cleanup**
#### `deleteFirestoreDoc()`
- Removes Firestore room data after the call ends.

### 7. **Firebase Listener**
#### `setupFirebaseListener()`
- Listens for signaling messages (SDP & ICE candidates) in Firestore and processes them.

### 8. **Creating Peer Connection**
#### `createPeerConnection()`
- Establishes WebRTC connection between peers using ICE servers.

---

## How It Works
1. **User joins a room** → Room capacity is checked.
2. **WebRTC initializes** → Creates `PeerConnectionFactory` and `PeerConnection`.
3. **SDP Offer/Answer Exchange** → The offerer sends an SDP offer, and the answerer replies with an SDP answer.
4. **ICE Candidate Exchange** → ICE candidates are shared via Firestore.
5. **Direct P2P Connection** → Once the connection is established, video streaming starts.
6. **Call Ends** → Firestore data is cleaned up.

---

## Technologies Used
- **WebRTC**: For peer-to-peer video calling.
- **Firebase Firestore**: As the signaling server.
- **Android (Kotlin/Java)**: For mobile application development.

---

## Future Improvements
- Implement TURN servers for better connectivity in restrictive networks.
- Add support for group video calls.
- Optimize ICE candidate exchange for faster connection setup.

---

## Contributing
Feel free to submit issues and pull requests for improvements.

---

## License
This project is licensed under the MIT License.

