package kr.dallyeobom.controller.temporalAuth.response

data class ServiceTokensResponse(
    val accessToken: String,
    val refreshToken: String,
)
