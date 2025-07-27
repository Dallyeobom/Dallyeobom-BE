package kr.dallyeobom.controller.auth.response

data class ServiceTokensResponse(
    val accessToken: String,
    val refreshToken: String,
)
