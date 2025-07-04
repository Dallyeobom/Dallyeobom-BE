package kr.dallyeobom.controller.course.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.controller.common.response.SimpleUserResponse
import kr.dallyeobom.entity.CourseCompletionHistory

data class CourseRankResponse(
    val user: SimpleUserResponse,
    @field:Schema(
        description = "사용자의 기록- 초단위",
        example = "1500",
    )
    val interval: Long,
) {
    companion object {
        fun from(courseCompletionHistory: CourseCompletionHistory): CourseRankResponse =
            CourseRankResponse(
                user = SimpleUserResponse.from(courseCompletionHistory.user),
                interval = courseCompletionHistory.interval.toSeconds(),
            )
    }
}
