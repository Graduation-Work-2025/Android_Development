package com.example.bloom.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object PreferenceManager {
    private const val PREF_NAME = "bloom_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_PROFILE_IMAGE_URL = "profile_image_url"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_USER_ID = "user_id"

    private lateinit var prefs: SharedPreferences

    /**
     * 초기화
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        Log.d("PreferenceManager", "SharedPreferences 초기화됨")
    }

    /**
     * ✅ Access Token 저장/조회/삭제
     */
    fun setAccessToken(token: String) {
        Log.d("PreferenceManager", "저장된 토큰: $token")
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        Log.d("PreferenceManager", "조회된 토큰: $token")
        return token
    }

    fun clearAccessToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
        Log.d("PreferenceManager", "토큰이 삭제됨")
    }

    /**
     * ✅ 유저 ID 저장/조회
     */
    fun setUserId(id: Int) {
        prefs.edit().putInt(KEY_USER_ID, id).apply()
        Log.d("PreferenceManager", "저장된 User ID: $id")
    }

    fun getUserId(): Int? {
        val id = prefs.getInt(KEY_USER_ID, -1)
        Log.d("PreferenceManager", "조회된 User ID: $id")
        return if (id == -1) null else id
    }

    /**
     * ✅ 닉네임 저장/조회 (로그 추가)
     */
    fun setNickname(nickname: String) {
        prefs.edit().putString(KEY_NICKNAME, nickname).apply()
        Log.d("PreferenceManager", "저장된 닉네임: $nickname")
    }

    fun getNickname(): String {
        val nickname = prefs.getString(KEY_NICKNAME, "사용자") ?: "사용자"
        Log.d("PreferenceManager", "조회된 닉네임: $nickname")
        return nickname
    }

    /**
     * ✅ 프로필 이미지 URL 저장/조회
     */
    fun setProfileImageUrl(url: String) {
        prefs.edit().putString(KEY_PROFILE_IMAGE_URL, url).apply()
        Log.d("PreferenceManager", "저장된 프로필 이미지 URL: $url")
    }

    fun getProfileImageUrl(): String {
        val url = prefs.getString(KEY_PROFILE_IMAGE_URL, "")
        Log.d("PreferenceManager", "조회된 프로필 이미지 URL: $url")
        return url ?: ""
    }

    /**
     * ✅ 로그아웃 시 모든 데이터 초기화
     */
    fun clearUserData() {
        prefs.edit().clear().apply()
        Log.d("PreferenceManager", "모든 사용자 데이터가 초기화되었습니다.")
    }
}
