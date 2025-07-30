package kr.dallyeobom.controller.courseCompletionHistory.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.dto.LatLngDto
import kr.dallyeobom.entity.CourseCompletionHistory

class CourseCompletionHistoryDetailResponse(
    @Schema(description = "코스 완료 이력 ID", example = "1")
    val id: Long = 0L,
    @Schema(description = "코스 ID", example = "1")
    val courseId: Long?,
    @Schema(description = "코스명", example = "장충동 산5 15 Climb")
    val courseName: String?,
    @Schema(description = "현재 유저가 해당 코스의 생성자 인지 여부, 코스 ID가 없으면 null - 이 값을 가지고 나중에 코스 수정 API 호출 가능 여부를 결정하면 됩니다", example = "true")
    val isCreator: Boolean?,
    @Schema(description = "유저 ID", example = "1")
    val userId: Long,
    @Schema(description = "코스 리뷰", example = "이 코스는 정말 좋았습니다! 다음에도 또 달리고 싶어요.")
    val review: String,
    @Schema(description = "소요시간 (초 단위)", example = "3600")
    val interval: Long,
    @Schema(description = "거리 (미터)", example = "5000")
    val length: Int,
    @Schema(
        description = "코스 경로",
        example = "[{\"latitude\": 37.5665, \"longitude\": 126.978}, {\"latitude\": 37.567, \"longitude\": 126.979}]",
    )
    val path: List<LatLngDto>,
    @Schema(description = "코스 완주 인증샷들", example = "[{\"id\":1, \"imageUrl\":\"https://example.com/image.jpg\"}]")
    val completionImages: List<CourseCompletionImageResponse>,
) {
    companion object {
        fun from(
            userId: Long,
            courseCompletionHistory: CourseCompletionHistory,
            imageUrls: List<CourseCompletionImageResponse>,
        ): CourseCompletionHistoryDetailResponse =
            CourseCompletionHistoryDetailResponse(
                id = courseCompletionHistory.id,
                courseId = courseCompletionHistory.course?.id,
                courseName = courseCompletionHistory.course?.name,
                isCreator = courseCompletionHistory.course?.let { it.creatorId == userId },
                userId = courseCompletionHistory.user.id,
                review = courseCompletionHistory.review,
                interval = courseCompletionHistory.interval.toSeconds(),
                path = courseCompletionHistory.path.coordinates.map { LatLngDto(it.x, it.y) },
                length = courseCompletionHistory.length,
                completionImages = imageUrls,
            )
    }

    data class CourseCompletionImageResponse(
        @Schema(description = "완주 인증샷 ID", example = "1")
        val id: Long,
        @Schema(description = "완주 인증샷 URL", example = "https://example.com/image.jpg")
        val imageUrl: String,
    )
}
