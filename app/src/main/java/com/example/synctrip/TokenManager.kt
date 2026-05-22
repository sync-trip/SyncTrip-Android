package com.example.synctrip

import android.content.Context
import android.content.SharedPreferences

object TokenManager {

    private const val PREF_NAME = "synctrip_prefs"
    private const val KEY_JWT = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_ACCESS_EXPIRES_AT = "access_token_expires_at"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_PROFILE_IMAGE = "user_profile_image"
    private const val KEY_USER_EMAIL = "user_email"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        prefs(context).edit().putString(KEY_JWT, token).apply()
    }

    fun getToken(context: Context): String? {
        return prefs(context).getString(KEY_JWT, null)
    }

    fun saveUserId(context: Context, userId: Long) {
        prefs(context).edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun getUserId(context: Context): Long {
        return prefs(context).getLong(KEY_USER_ID, -1L)
    }

    // 로그인 응답 전체 저장 (refreshToken + 만료시각 포함)
    fun saveLoginResponse(context: Context, accessToken: String, refreshToken: String,
                          userId: Long, accessTokenExpiresIn: Long,
                          userName: String? = null, profileImageUrl: String? = null,
                          email: String? = null) {
        prefs(context).edit().apply {
            putString(KEY_JWT, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_USER_ID, userId)
            putLong(KEY_ACCESS_EXPIRES_AT, System.currentTimeMillis() + accessTokenExpiresIn * 1000)
            if (userName != null) putString(KEY_USER_NAME, userName)
            if (profileImageUrl != null) putString(KEY_USER_PROFILE_IMAGE, profileImageUrl)
            if (email != null) putString(KEY_USER_EMAIL, email)
        }.apply()
    }

    fun getUserName(context: Context): String? {
        return prefs(context).getString(KEY_USER_NAME, null)
    }

    fun getProfileImageUrl(context: Context): String? {
        return prefs(context).getString(KEY_USER_PROFILE_IMAGE, null)
    }

    fun getUserEmail(context: Context): String? {
        return prefs(context).getString(KEY_USER_EMAIL, null)
    }

    fun getRefreshToken(context: Context): String? {
        return prefs(context).getString(KEY_REFRESH_TOKEN, null)
    }

    // 1분 여유를 두고 만료 여부 판단
    fun isAccessTokenExpired(context: Context): Boolean {
        val expiresAt = prefs(context).getLong(KEY_ACCESS_EXPIRES_AT, 0L)
        if (expiresAt == 0L) return false // 기존 사용자 (만료시각 저장 안 됨) → 만료로 보지 않음
        return System.currentTimeMillis() >= expiresAt - 60_000L
    }

    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}