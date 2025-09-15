package kr.dallyeobom.controller.courseCompletionHistory.request

import com.google.maps.model.LatLng
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import kr.dallyeobom.dto.LatLngDto
import kr.dallyeobom.entity.CourseLevel
import kr.dallyeobom.entity.CourseVisibility
import org.hibernate.validator.constraints.Length

data class CourseCompletionCreateRequest(
    @field:Schema(
        description = "코스 ID - 코스 선택 후 달린 경우에만 입력",
        example = "1",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    @field:Positive(message = "코스 ID는 양수여야 합니다.")
    val courseId: Long?,
    @field:Schema(description = "코스 리뷰", example = "이 코스는 정말 좋았습니다! 다음에도 또 달리고 싶어요.")
    @field:Length(min = 1, max = 300, message = "리뷰는 최소 1자, 최대 300자까지 입력 가능합니다.")
    val review: String?,
    @field:Schema(description = "소요시간 (초 단위)", example = "3600")
    @field:Positive(message = "소요시간은 양수여야 합니다.")
    val interval: Long,
    @field:Schema(
        description = "코스 경로",
        example = "[{\"latitude\": 37.5665, \"longitude\": 126.978}, {\"latitude\": 37.567, \"longitude\": 126.979}]",
    )
    @field:Size(min = 1, message = "경로는 최소 1개의 좌표가 필요합니다.")
    val path: List<LatLngDto>,
    @field:Schema(
        description = "완주한 코스 공개 설정 - courseId가 null인 경우에만 입력",
        example = "PUBLIC",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val courseVisibility: CourseVisibility?,
    @field:Schema(
        description = "코스 생성 정보 - courseId가 null이고 courseVisibility가 PRIVATE가 아닌 경우에만 입력",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val courseCreateInfo: CourseCreateInfo?,
) {
    @Schema(hidden = true)
    val latLngPath: List<LatLng> = path.map(LatLngDto::toLatLng)
}

data class CourseCreateInfo(
    @field:Schema(description = "코스 설명", example = "서울 한강을 따라 자전거를 탈 수 있는 코스입니다.")
    @field:Length(min = 1, max = 500, message = "코스 설명은 최소 1자, 최대 500자까지 입력 가능합니다.")
    val description: String,
    @field:Schema(description = "코스명", example = "서울 한강 러닝 코스")
    @field:Length(min = 1, max = 30, message = "코스명은 최소 1자, 최대 30자까지 입력 가능합니다.")
    val name: String,
    @field:Schema(description = "코스 난이도", example = "LOW")
    val courseLevel: CourseLevel,
)
