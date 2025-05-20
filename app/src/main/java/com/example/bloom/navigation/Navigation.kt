package com.example.bloom.navigation

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bloom.screen.*

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            if (!granted) {
                Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // ✅ StartScreen이 첫 화면이 되도록 설정
    NavHost(navController = navController, startDestination = "start") {

        composable("start") {
            StartScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("signup") {
            SignUpScreen(navController)
        }

        composable("main") {
            MainScreen(navController = navController)
        }

        // ✅ Create Post Screen
        composable("create_post") {
            CreatePostScreen(navController)
        }

        // ✅ Post Detail Screen - Int 타입으로 통일
        composable(
            "post_detail/{storyId}",
            arguments = listOf(navArgument("storyId") { type = NavType.IntType })
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getInt("storyId")
            PostDetailScreen(navController = navController, storyId = storyId)
        }

        // ✅ ChatGPT Test Screen
        composable("chatgpttest") {
            ChatGptTestScreen(navController)
        }

        // ✅ Emotion Garden Screen
        composable("emotion_garden") {
            MyFeedGardenScreen(navController)
        }
    }
}