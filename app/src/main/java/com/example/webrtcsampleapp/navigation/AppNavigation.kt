package com.example.webrtcsampleapp.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.webrtcsampleapp.screens.HomeScreen
import com.example.webrtcsampleapp.screens.PermissionScreen
import com.example.webrtcsampleapp.screens.VideoCallScreen
import timber.log.Timber

@Composable
fun WebRTCAppNavigation(){
    CompositionLocalProvider(
        LocalNavController provides rememberNavController()
    ) {
        SetupNavigation()
    }
}

@Composable
fun SetupNavigation() {
    val navController = LocalNavController.current
    val context = LocalContext.current

    val isCameraPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    NavHost(
        navController = navController,
        startDestination = if (isCameraPermissionGranted) "Home" else "Permission"
    ){
        composable("Home"){
            HomeScreen(navigateToVideoCallScreen = { roomID ->
                navController.navigate("VideoCallScreen/$roomID")

            })
        }
        composable("Permission"){
            PermissionScreen(navigateToHomeScreen = {
                navController.navigate("Home"){
                    popUpTo(0){
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            })
        }
        composable("VideoCallScreen/{roomID}"){ backStackEntry ->
            val roomID = backStackEntry.arguments?.getString("roomID", "")
            if (roomID.isNullOrEmpty()){
                Timber.e("Room ID not Available")
                return@composable
            }
            VideoCallScreen(roomID)
        }


    }

}

val LocalNavController = compositionLocalOf<NavHostController> { error("no LocalNavController Provided!") }