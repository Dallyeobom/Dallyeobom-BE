package kr.dallyeobom.controller.course.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.controller.common.response.SimpleUserResponse
import kr.dallyeobom.entity.UserRunningCourse

data class NearByUserRunningCourseResponse(
    @Schema(description = "코스 ID", example = "1")
    val id: Long,
    @Schema(description = "코스명", example = "장충동 산5 15 Climb")
    val name: String,
    @Schema(description = "코스 대표 사진", example = "https://example.com/image.jpg")
    val courseImage: String?,
    val user: SimpleUserResponse,
) {
    companion object {
        fun from(
            userRunningCourse: UserRunningCourse,
            courseImage: String?,
            userProfileImage: String?,
        ) = NearByUserRunningCourseResponse(
            id = userRunningCourse.course.id,
            name = userRunningCourse.course.name,
            courseImage = courseImage,
            user = SimpleUserResponse.from(userRunningCourse.user, userProfileImage),
        )
    }
}
