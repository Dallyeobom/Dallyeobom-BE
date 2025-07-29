package kr.dallyeobom.controller.user.response

import io.swagger.v3.oas.annotations.media.Schema

data class UserInfoResponse(
    @Schema(description = "유저의 닉네임", example = "홍길동")
    val nickname: String,
    @Schema(description = "프로필 이미지", example = "프로필 이미지 url")
    val profileImage: String?,
)
