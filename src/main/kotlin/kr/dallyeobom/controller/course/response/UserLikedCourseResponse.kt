package kr.dallyeobom.controller.course.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.entity.CourseLevel
import kr.dallyeobom.entity.CourseLikeHistory

data class UserLikedCourseResponse(
    @Schema(description = "좋아요 ID", example = "1")
    val id: Long,
    @Schema(description = "코스 ID", example = "1")
    val courseId: Long,
    @Schema(description = "코스명", example = "장충동 산5 15 Climb")
    val name: String,
    @Schema(description = "코스 썸네일(지도상에 코스 간략하게 나오는 사진)", example = "https://example.com/image.jpg")
    val overViewImage: String,
    @Schema(description = "코스 길이 (미터 단위)", example = "15000")
    val length: Int,
    @Schema(description = "난이도", example = "MIDDLE")
    val level: CourseLevel,
    @Schema(description = "좋아요 여부", example = "true")
    val isLiked: Boolean,
) {
    companion object {
        fun from(
            courseLikeHistory: CourseLikeHistory,
            overViewImage: String,
            isLiked: Boolean,
        ) = UserLikedCourseResponse(
            id = courseLikeHistory.id,
            courseId = courseLikeHistory.course.id,
            name = courseLikeHistory.course.name,
            overViewImage = overViewImage,
            length = courseLikeHistory.course.length,
            level = courseLikeHistory.course.courseLevel,
            isLiked = isLiked,
        )
    }
}
