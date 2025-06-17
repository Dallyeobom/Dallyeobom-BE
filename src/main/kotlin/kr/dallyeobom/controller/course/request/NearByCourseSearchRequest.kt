package kr.dallyeobom.controller.course.request

import io.swagger.v3.oas.annotations.Parameter

data class NearByCourseSearchRequest(
    @field:Parameter(description = "위도", example = "37.5665")
    val latitude: Double,
    @field:Parameter(description = "경도", example = "126.978")
    val longitude: Double,
    @field:Parameter(description = "반경 (미터 단위) - 값이 없으면 1000", example = "1000")
    val radius: Int = 1000,
    @field:Parameter(description = "기대하는 응답 개수 - 값이 없으면 10", example = "10")
    val maxCount: Int = 10,
)
