package kr.dallyeobom.controller.courseCompletionHistory.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.entity.CourseCompletionHistory

data class CourseCompletionCreateResponse(
    @Schema(description = "등록된 완주기록 ID", example = "1")
    val id: Long,
) {
    companion object {
        fun from(courseCompletionHistory: CourseCompletionHistory) = CourseCompletionCreateResponse(courseCompletionHistory.id)
    }
}
