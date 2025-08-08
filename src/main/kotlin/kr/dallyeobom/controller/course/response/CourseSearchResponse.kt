package kr.dallyeobom.controller.course.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseLevel

data class CourseSearchResponse(
    @Schema(description = "코스 ID", example = "1")
    val id: Long,
    @Schema(description = "코스명", example = "장충동 산5 15 Climb")
    val name: String,
    @Schema(description = "지역", example = "영등포구 대림동")
    val location: String,
    @Schema(description = "코스 썸네일(지도상에 코스 간략하게 나오는 사진)", example = "https://example.com/image.jpg")
    val overViewImageUrl: String,
    @Schema(description = "코스 길이 (미터 단위)", example = "15000")
    val length: Int,
    @Schema(description = "난이도", example = "MIDDLE")
    val level: CourseLevel,
    @Schema(description = "좋아요 여부", example = "true")
    val isLiked: Boolean,
) {
    companion object {
        fun from(
            course: Course,
            overViewImageUrl: String,
            isLiked: Boolean,
        ) = CourseSearchResponse(
            id = course.id,
            name = course.name,
            location = course.location,
            overViewImageUrl = overViewImageUrl,
            length = course.length,
            level = course.courseLevel,
            isLiked = isLiked,
        )
    }
}
