package com.example.testappjsh

import android.content.Context
import android.content.SharedPreferences

object TokenManager {

    private const val PREF_NAME = "synctrip_prefs"
    private const val KEY_JWT = "jwt_token"
    private const val KEY_USER_ID = "user_id"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // JWT 저장
    fun saveToken(context: Context, token: String) {
        prefs(context).edit().putString(KEY_JWT, token).apply()
    }

    // JWT 불러오기
    fun getToken(context: Context): String? {
        return prefs(context).getString(KEY_JWT, null)
    }

    // 유저 ID 저장
    fun saveUserId(context: Context, userId: Long) {
        prefs(context).edit().putLong(KEY_USER_ID, userId).apply()
    }

    // 유저 ID 불러오기
    fun getUserId(context: Context): Long {
        return prefs(context).getLong(KEY_USER_ID, -1L)
    }

    // 로그인 여부 확인
    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }

    // 로그아웃 (토큰 삭제)
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}