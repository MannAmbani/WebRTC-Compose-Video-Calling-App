package com.example.webrtcsampleapp.screens

import android.telecom.InCallService
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun VideoCallScreen(roomID: String){
    Text("Hello from video call screen - $roomID!!")
}