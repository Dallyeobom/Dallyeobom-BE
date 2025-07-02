package kr.dallyeobom.controller.course.response

import io.swagger.v3.oas.annotations.media.Schema

data class CourseLikeResponse(
    @Schema(description = "코스를 좋아요/좋아요취소 여부", example = "true")
    val isLiked: Boolean,
    @Schema(description = "코스의 현재 좋아요 수", example = "123")
    val likeCount: Int,
)
