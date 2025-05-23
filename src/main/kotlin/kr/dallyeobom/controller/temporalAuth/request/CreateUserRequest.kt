package kr.dallyeobom.controller.temporalAuth.request

import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Length

data class CreateUserRequest(
    @Schema(description = "유저의 닉네임을 입력해주세요. 2자 이상 20자 이하로 입력 가능합니다", example = "라이언")
    @field:Length(min = 2, max = 20)
    val nickName: String,
)
