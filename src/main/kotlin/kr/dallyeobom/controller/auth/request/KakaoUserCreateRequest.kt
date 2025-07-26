package kr.dallyeobom.controller.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class KakaoUserCreateRequest(
    @field:NotBlank(message = "닉네임은 필수입니다.")
    @field:Size(min = 2, max = 15, message = "닉네임은 2자부터 15자이하로 입력 가능합니다.")
    @field:Schema(
        example = "닉네임",
    )
    val nickname: String,
    @field:NotBlank(message = "provider 엑세스 토큰은 필수입니다.")
    @field:Schema(
        example = "provider 액세스 토큰",
    )
    val providerAccessToken: String,
    @field:NotEmpty(message = "약관 동의 항목은 필수입니다.")
    @Schema(
        example = """
      [
        {
          "id": 1,
          "termsType": "SERVICE",
          "agreed": true
        },
        {
          "id": 2,
          "termsType": "PRIVACY",
          "agreed": true
        },
        {
          "id": 3,
          "termsType": "PUSH",
          "agreed": false
        }
      ]
    """,
    )
    val terms: List<TermsAgreeRequest>,
)
