package com.example.bloom

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 진입 확인 로그 및 토스트
        Log.d("🌸SplashCheck", "SplashActivity 진입 성공!")
        //Toast.makeText(this, "✅ SplashActivity 실행됨", Toast.LENGTH_SHORT).show()

        // 레이아웃 설정
        setContentView(R.layout.activity_splash)

        // 2초 뒤에 메인화면 이동
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}
