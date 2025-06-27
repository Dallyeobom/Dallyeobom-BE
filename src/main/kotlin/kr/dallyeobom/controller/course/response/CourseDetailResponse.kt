package kr.dallyeobom.controller.course.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.dto.LatLngDto
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseLevel

data class CourseDetailResponse(
    @Schema(description = "코스 ID", example = "1")
    val id: Long,
    @Schema(description = "코스명", example = "장충동 산5 15 Climb")
    var name: String,
    @Schema(description = "코스 설명", example = "장충동 산5 15 Climb 코스는 서울의 아름다운 경치를 감상할 수 있는 코스입니다.")
    var description: String,
    @Schema(description = "코스 난이도", example = "LOW")
    var courseLevel: CourseLevel,
    @Schema(description = "코스 대표 이미지", example = "https://example.com/image.jpg")
    var imageUrl: String?,
    @Schema(description = "코스명", example = "장충동 산5 15 Climb")
    var location: String,
    @Schema(description = "코스 썸네일(지도상에 코스 간략하게 나오는 사진)", example = "https://example.com/image.jpg")
    var overViewImageUrl: String,
    @Schema(description = "코스 길이 (미터 단위)", example = "15000")
    val length: Int,
    val path: List<LatLngDto>,
    @Schema(description = "현재 유저가 해당 코스의 생성자 인지 여부 - 이 값을 가지고 나중에 코스 수정 API 호출 가능 여부를 결정하면 됩니다", example = "true")
    val isCreator: Boolean?,
) {
    companion object {
        fun from(
            userId: Long,
            course: Course,
            imageUrl: String?,
            overViewImageUrl: String,
        ) = CourseDetailResponse(
            id = course.id,
            name = course.name,
            description = course.description,
            courseLevel = course.courseLevel,
            imageUrl = imageUrl,
            location = course.location,
            overViewImageUrl = overViewImageUrl,
            length = course.length,
            path = course.path.coordinates.map { LatLngDto(it.x, it.y) },
            isCreator = course.creatorId == userId,
        )
    }
}
