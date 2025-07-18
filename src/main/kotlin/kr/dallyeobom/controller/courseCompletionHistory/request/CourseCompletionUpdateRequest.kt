package kr.dallyeobom.controller.courseCompletionHistory.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Length

data class CourseCompletionUpdateRequest(
    @field:Schema(description = "코스 리뷰", example = "이 코스는 정말 좋았습니다! 다음에도 또 달리고 싶어요.")
    @field:Length(min = 1, max = 300, message = "리뷰는 최소 1자, 최대 300자까지 입력 가능합니다. null이면 기존 리뷰를 유지합니다.")
    val review: String?,
    @field:Schema(description = "지우고자 하는 코스 인증샷 ID 리스트 - null이면 기존 인증샷을 유지합니다.", example = "[1, 3]")
    @field:Size(max = 3, message = "인증샷은 최대 3개까지 삭제할 수 있습니다.")
    val deleteImageIds: Set<Long>?,
)
