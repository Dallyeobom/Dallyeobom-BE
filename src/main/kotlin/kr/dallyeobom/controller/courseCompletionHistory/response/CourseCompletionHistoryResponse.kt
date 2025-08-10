package kr.dallyeobom.controller.courseCompletionHistory.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.dto.LatLngDto
import kr.dallyeobom.entity.CourseCompletionHistory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CourseCompletionHistoryResponse(
    @Schema(description = "코스 완주 기록 ID", example = "1")
    val id: Long,
    @Schema(description = "연결된 코스 ID", example = "1")
    val courseId: Long?,
    @Schema(description = "코스명 혹은 기록명", example = "장충동 산5 15 Climb | 8월 2일 기록")
    val title: String,
    @Schema(description = "걸린시간 (초)", example = "3600")
    val interval: Long,
    @Schema(description = "거리 (미터)", example = "5000")
    val length: Int,
    @Schema(description = "완주 일자", example = "2025-08-02")
    val completeDate: LocalDate,
    @Schema(
        description = "코스 경로",
        example = "[{\"latitude\": 37.5665, \"longitude\": 126.978}, {\"latitude\": 37.567, \"longitude\": 126.979}]",
    )
    val path: List<LatLngDto>,
) {
    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 기록")

        fun from(item: CourseCompletionHistory): CourseCompletionHistoryResponse =
            CourseCompletionHistoryResponse(
                id = item.id,
                courseId = item.course?.id,
                title = item.course?.name ?: dateTimeFormatter.format(item.createdDateTime),
                interval = item.interval.toSeconds(),
                length = item.length,
                completeDate = item.createdDateTime.toLocalDate(),
                path = item.path.coordinates.map { LatLngDto(it.x, it.y) },
            )
    }
}
