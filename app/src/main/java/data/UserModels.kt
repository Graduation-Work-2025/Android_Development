package com.example.bloom.data

// 로그인 요청
data class LoginRequest(
    val domain: String = "user",
    val command: String = "login",
    val request: LoginInfo
)

data class LoginInfo(
    val user_id: String,
    val password: String
)

// 로그인 응답
data class LoginResponse(
    val error: ErrorResponse,
    val response: LoginSuccessData?
)

data class UserData(
    val id: Int,
    val name: String,
    val nickname: String,
    val user_id: String,
    val phone: String
)

data class LoginSuccessData(
    val access_token: String
)

// 회원가입 요청
data class SignUpRequest(
    val domain: String = "user",
    val command: String = "sign_up",
    val token: String = "",
    val request: SignUpInfo
)

data class SignUpInfo(
    val name: String,
    val nickname: String,
    val user_id: String,
    val password: String,
    val phone: String,
    val character_id: Int
)


// 회원가입 응답
data class SignUpResponse(
    val error: ErrorResponse,
    val response: Any? // 회원가입은 응답이 null
)




// 공통 에러 응답
data class ErrorResponse(
    val code: Int,
    val message: String?
)
