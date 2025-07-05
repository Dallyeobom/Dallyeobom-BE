package kr.dallyeobom.controller.common.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.entity.User

// 간단한 유저 응답 객체
data class SimpleUserResponse(
    @field:Schema(description = "유저 ID", example = "1")
    val id: Long,
    @field:Schema(description = "유저 닉네임", example = "홍길동")
    val nickname: String,
) {
    companion object {
        fun from(user: User) =
            SimpleUserResponse(
                id = user.id,
                nickname = user.nickname,
            )
    }
}
