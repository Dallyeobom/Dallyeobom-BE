package kr.dallyeobom.controller.auth.request

data class KakaoUserCreateRequest(
    val nickName: String,
    val providerAccessToken: String,
)
