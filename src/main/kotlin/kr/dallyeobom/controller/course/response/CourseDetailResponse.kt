package kr.dallyeobom.controller.course.response

import io.swagger.v3.oas.annotations.media.Schema
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
    val path: List<LatLng>,
) {
    companion object {
        fun from(
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
            path = course.path.coordinates.map { LatLng(it.x, it.y) },
        )
    }

    data class LatLng(
        @Schema(description = "위도", example = "37.5665")
        val latitude: Double,
        @Schema(description = "경도", example = "126.978")
        val longitude: Double,
    )
}
