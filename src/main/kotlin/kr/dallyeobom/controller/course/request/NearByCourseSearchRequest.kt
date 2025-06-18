package kr.dallyeobom.controller.course.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive
import org.hibernate.validator.constraints.Range

data class NearByCourseSearchRequest(
    @field:Schema(description = "위도", example = "37.5665")
    @field:Range(min = -90, max = 90, message = "위도는 -90에서 90 사이의 값이어야 합니다.")
    val latitude: Double,
    @field:Schema(description = "경도", example = "126.978")
    @field:Range(min = -180, max = 180, message = "경도는 -180에서 180 사이의 값이어야 합니다.")
    val longitude: Double,
    @field:Schema(
        description = "검색 범위 (미터 단위) - 값이 없으면 1000",
        example = "1000",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    @field:Positive(message = "검색 범위는 양수여야 합니다.")
    val radius: Int = 1000,
    @field:Schema(
        description = "기대하는 응답 개수 - 값이 없으면 10",
        example = "10",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    @field:Positive(message = "최대 응답 개수는 양수여야 합니다.")
    val maxCount: Int = 10,
)
