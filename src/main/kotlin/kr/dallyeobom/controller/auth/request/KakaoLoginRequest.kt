package kr.dallyeobom.controller.auth.request

import jakarta.validation.constraints.NotBlank

data class KakaoLoginRequest(
    @field:NotBlank(message = "provider 엑세스 토큰은 필수입니다.")
    val providerAccessToken: String,
)
