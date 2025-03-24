package com.example.webrtcsampleapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.firestore.FirebaseFirestore
import org.webrtc.SurfaceViewRenderer
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(roomID: String, onNavigateBack: () -> Unit) {
    //plan
    //First we will craete a room.
    //Now room is present
    //-- we will check the capacity of the room
    //-- more then 2 -> left back to home.


    val firestore = remember { FirebaseFirestore.getInstance() }
    val context = LocalContext.current

    fun checkRoomCapacity(onProceed: () -> Unit) {
        //getting rooms from room id
        val roomDocRef = firestore.collection("rooms").document(roomID)
        roomDocRef.get().addOnSuccessListener { document ->
            Timber.d("Firebase firestore success")
            //check if room exists
            if (document != null && document.exists()) {
                val participantCount = document.getLong("participantCount")?.toInt() ?: 0
                //if room is full
                if (participantCount >= 2) {
                    Toast.makeText(
                        context,
                        "Room is Full. Cannot join at the moment!",
                        Toast.LENGTH_SHORT
                    ).show()

                    //back to home page
                    onNavigateBack.invoke()

                } else {
                    //add another participant
                    roomDocRef.update("participantCount", participantCount + 1)
                }
            } else {
                //create room.
                roomDocRef.set(mapOf("participantCount" to 1))
            }
        }.addOnFailureListener {
            // Handle the error
            Timber.e("Firebase Failed to get Firestore DB.")
            Toast.makeText(
                context,
                "Failed to connect to Firebase. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
            onNavigateBack.invoke()
        }
    }

    //calls first for one time
    LaunchedEffect(Unit) {
        checkRoomCapacity(onProceed = {
            //all business logic will be here
        })
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary
            ), title = {
                Text(text = "WebRTC Sample!", fontWeight = FontWeight.Bold)
            })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
        ) {
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context)

                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context)

                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }
    }
}