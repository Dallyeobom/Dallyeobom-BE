package kr.dallyeobom.controller.auth.request

data class UserCreateRequest(
    val nickName: String,
    val accessToken: String,
)
