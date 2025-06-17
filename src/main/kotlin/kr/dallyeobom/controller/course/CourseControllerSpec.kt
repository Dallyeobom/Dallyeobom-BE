package kr.dallyeobom.controller.course

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dallyeobom.config.swagger.SwaggerTag
import kr.dallyeobom.controller.course.request.NearByCourseSearchRequest
import kr.dallyeobom.controller.course.response.CourseDetailResponse
import kr.dallyeobom.controller.course.response.NearByCourseSearchResponse
import org.springdoc.core.annotations.ParameterObject

@Tag(name = SwaggerTag.COURSE)
interface CourseControllerSpec {
    @Operation(
        summary = "관광데이터API를 통해 코스 생성",
        description = "관광데이터API를 통해 코스를 생성합니다. 해당 API는 1회성으로, DB에 관광데이터가 이미 존재한다면 사용하지 않습니다.",
        responses = [
            ApiResponse(responseCode = "201", description = "코스 생성 성공"),
        ],
    )
    fun insertTourApiCourse()

    @Operation(
        summary = "주변 코스 검색",
        description = "주변 코스를 검색합니다. 위도, 경도, 반경, 최대 응답 개수를 입력받아 해당 조건에 맞는 코스를 반환합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "주변 코스 검색 결과"),
        ],
    )
    fun searchNearByCourse(
        @ParameterObject request: NearByCourseSearchRequest,
    ): List<NearByCourseSearchResponse>

    @Operation(
        summary = "코스 상세 조회",
        description = "코스 ID를 입력받아 해당 코스의 상세 정보를 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "코스 상세 정보"),
        ],
    )
    fun getCourseDetail(
        @Schema(description = "상세조회 하고자 하는 코스의 ID", example = "1")
        id: Long,
    ): CourseDetailResponse
}
