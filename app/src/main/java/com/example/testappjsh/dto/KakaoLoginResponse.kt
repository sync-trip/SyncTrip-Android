package com.example.testappjsh.dto

data class KakaoLoginResponse(
    val userId: Long,
    val email: String?,
    val name: String?,
    val profileImageUrl: String?,
    val newUser: Boolean,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long
)