package kr.dallyeobom.controller.courseCompletionHistory.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.entity.CourseCompletionHistory

data class CourseCompletionHistoryResponse(
    @Schema(description = "코스 완주 기록 ID", example = "1")
    val id: Long,
    @Schema(description = "연결된 코스 ID", example = "1")
    val courseId: Long?,
    @Schema(description = "걸린시간 (초)", example = "3600")
    val interval: Long,
    @Schema(description = "거리 (미터)", example = "5000")
    val length: Int,
    @Schema(description = "코스 완주 인증샷", example = "https://example.com/image.jpg")
    val completionImage: String?,
    @Schema(description = "좋아요 여부", example = "true")
    val isLiked: Boolean,
) {
    companion object {
        fun from(
            item: CourseCompletionHistory,
            imageUrl: String?,
            isLiked: Boolean,
        ): CourseCompletionHistoryResponse =
            CourseCompletionHistoryResponse(
                id = item.id,
                courseId = item.course?.id,
                interval = item.interval.toSeconds(),
                length = item.length,
                completionImage = imageUrl,
                isLiked = isLiked,
            )
    }
}
