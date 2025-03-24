package com.example.webrtcsampleapp.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.webrtcsampleapp.ui.theme.Red700
import com.example.webrtcsampleapp.ui.theme.Teal700
import com.example.webrtcsampleapp.ui.theme.WebRTCSampleAppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(navigateToHomeScreen: () -> Unit) {
    val context = LocalContext.current
    val permission = rememberMultiplePermissionsState(listOf(Manifest.permission.CAMERA))
    if (permission.allPermissionsGranted){
        navigateToHomeScreen.invoke()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Let's get you connected!", fontSize = 24.sp,
                fontWeight = FontWeight.Bold, color = Teal700
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "To start a video call, we need access to your camera and microphone",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                permission.launchMultiplePermissionRequest()
            }, colors = ButtonDefaults.buttonColors(containerColor = Teal700)) {
                Text(text = "Grant Permissions Now!")
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Why do we need those these permissions ?",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Camera: To share your smiling face with others in video calls.",
                        fontSize = 14.sp,
                        color = Teal700
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Microphone: So others can hear your voice loud and clear.",
                        fontSize = 14.sp,
                        color = Teal700
                    )
                }

            }

            Text(
                text = "If you denied the permission or want to enable it later, no worries!",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Button(onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    ).apply {
                        data = Uri.fromParts("package",context.packageName,null)
                        addCategory(Intent.CATEGORY_DEFAULT)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    }
                )
            }, colors = ButtonDefaults.buttonColors(containerColor = Red700)) {
                Text(text = "Open App Settings")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPermissionScreen() {
    WebRTCSampleAppTheme {
        PermissionScreen({})
    }
}