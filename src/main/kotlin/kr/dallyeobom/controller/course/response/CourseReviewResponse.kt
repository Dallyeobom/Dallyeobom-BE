package kr.dallyeobom.controller.course.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.controller.common.response.SimpleUserResponse
import kr.dallyeobom.entity.CourseCompletionHistory
import java.time.format.DateTimeFormatter

data class CourseReviewResponse(
    @Schema(description = "코스 리뷰 ID", example = "1")
    val id: Long,
    @Schema(description = "리뷰 작성자 정보")
    val user: SimpleUserResponse,
    @Schema(description = "코스 리뷰 내용", example = "정말 좋은 코스였습니다!")
    val review: String,
    @Schema(
        description = "코스 완료 이미지 URL 목록 - 없으면 null",
        example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]",
    )
    val completionImages: List<String>?,
    @Schema(description = "리뷰 작성 날짜", example = "2025.07.01")
    val createdAt: String,
) {
    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

        fun from(
            courseCompletionHistory: CourseCompletionHistory,
            profileImage: String?,
            completionImages: List<String>?,
        ) = CourseReviewResponse(
            id = courseCompletionHistory.id,
            user = SimpleUserResponse.from(courseCompletionHistory.user, profileImage),
            review = courseCompletionHistory.review,
            completionImages = completionImages,
            createdAt = dateTimeFormatter.format(courseCompletionHistory.createdDateTime),
        )
    }
}
