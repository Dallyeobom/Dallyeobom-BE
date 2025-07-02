package kr.dallyeobom.controller.auth.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class KakaoUserCreateRequest(
    @field:NotBlank(message = "닉네임은 필수입니다.")
    @field:Size(min = 2, max = 15, message = "닉네임은 2자부터 15자이하로 입력 가능합니다.")
    val nickName: String,
    @field:NotBlank(message = "provider 엑세스 토큰은 필수입니다.")
    val providerAccessToken: String,
    @field:NotEmpty(message = "약관 동의 항목은 필수입니다.")
    val terms: List<TermsAgreeRequest>,
)
