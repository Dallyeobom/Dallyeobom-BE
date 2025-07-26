package kr.dallyeobom.controller.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class NicknameUpdateRequest(
    @field:NotBlank(message = "닉네임은 필수입니다.")
    @field:Size(min = 2, max = 15, message = "닉네임은 2자부터 15자이하로 입력 가능합니다.")
    @field:Schema(
        example = "닉네임",
    )
    val nickname: String,
)
