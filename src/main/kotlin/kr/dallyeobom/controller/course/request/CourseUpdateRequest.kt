package kr.dallyeobom.controller.course.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.entity.CourseLevel
import kr.dallyeobom.entity.CourseVisibility
import org.hibernate.validator.constraints.Length

data class CourseUpdateRequest(
    @field:Schema(description = "코스 설명", example = "서울 한강을 따라 자전거를 탈 수 있는 코스입니다.")
    @field:Length(min = 1, max = 500, message = "코스 설명은 최소 1자, 최대 500자까지 입력 가능합니다.")
    val description: String?,
    @field:Schema(description = "코스명", example = "서울 한강 러닝 코스")
    @field:Length(min = 1, max = 30, message = "코스명은 최소 1자, 최대 30자까지 입력 가능합니다.")
    val name: String?,
    @field:Schema(description = "코스 난이도", example = "LOW")
    val courseLevel: CourseLevel?,
    @field:Schema(description = "코스 공개 설정", example = "PUBLIC")
    val courseVisibility: CourseVisibility? = null,
)
