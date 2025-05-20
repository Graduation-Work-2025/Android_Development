package com.example.bloom.util

import android.content.Context
import android.util.Base64
import android.util.Log

object TokenProvider {
    private const val PREF_NAME = "auth_pref"
    private const val KEY_TOKEN = "auth_token"
    private const val TAG = "TokenProvider"

    fun setToken(token: String, context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        Log.d(TAG, "저장할 토큰: $token")
        prefs.edit().putString(KEY_TOKEN, token).apply()

        // 저장 후 바로 확인
        val savedToken = prefs.getString(KEY_TOKEN, null)
        Log.d(TAG, "저장된 토큰 확인 (즉시): $savedToken")
    }

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_TOKEN, null)

        // SharedPreferences 객체 확인
        if (prefs == null) {
            Log.e(TAG, "SharedPreferences가 null입니다.")
        } else {
            Log.d(TAG, "SharedPreferences 객체가 정상적으로 생성됨")
        }

        // 토큰 확인 로그
        Log.d(TAG, "조회된 토큰: $token")

        return token
    }

    /**
     * ✅ 토큰 유효성 검증 함수
     */
    fun isTokenValid(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = parts[1]
                val decodedPayload = String(Base64.decode(payload, Base64.DEFAULT))
                Log.d(TAG, "JWT Payload: $decodedPayload")

                // "exp" 필드가 있는지 확인
                !decodedPayload.contains("exp") || !decodedPayload.contains("iat")
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "토큰 파싱 에러: ${e.message}")
            false
        }
    }
}
