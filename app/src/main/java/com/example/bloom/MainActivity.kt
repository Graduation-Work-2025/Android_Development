package com.example.bloom

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bloom.navigation.AppNavigator
import com.example.bloom.ui.theme.BloomTheme
import com.example.bloom.util.PreferenceManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ PreferenceManager 초기화
        PreferenceManager.init(applicationContext)

        // ✅ AccessToken 로그 출력
        val token = PreferenceManager.getAccessToken()
        Log.d("MainActivity", "onCreate - AccessToken: $token")

        enableEdgeToEdge()
        setContent {
            BloomTheme {
                AppNavigator()
            }
        }
    }
}
