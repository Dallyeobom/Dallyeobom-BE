package kr.dallyeobom.controller.course

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Positive
import kr.dallyeobom.config.swagger.SwaggerTag
import kr.dallyeobom.controller.course.request.CourseUpdateRequest
import kr.dallyeobom.controller.course.request.NearByCourseSearchRequest
import kr.dallyeobom.controller.course.response.CourseDetailResponse
import kr.dallyeobom.controller.course.response.CourseLikeResponse
import kr.dallyeobom.controller.course.response.NearByCourseSearchResponse
import kr.dallyeobom.util.validator.MaxFileSize
import org.springdoc.core.annotations.ParameterObject
import org.springframework.validation.annotation.Validated
import org.springframework.web.multipart.MultipartFile

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
            ApiResponse(
                responseCode = "400",
                description = """
        잘못된 요청:
        • 위도 또는 경도가 범위를 벗어남  
        • 검색 반경이 양수가 아님  
        • 최대 응답 개수가 양수가 아님  
      """,
                content = arrayOf(Content()),
            ),
        ],
    )
    fun searchNearByLocation(
        @Validated
        @ParameterObject request: NearByCourseSearchRequest,
    ): List<NearByCourseSearchResponse>

    @Operation(
        summary = "코스 상세 조회",
        description = "코스 ID를 입력받아 해당 코스의 상세 정보를 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "코스 상세 정보"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 - 코스 ID가 양수가 아님", content = arrayOf(Content())),
            ApiResponse(responseCode = "404", description = "ID에 해당하는 코스가 존재하지 않음", content = arrayOf(Content())),
        ],
    )
    fun getCourseDetail(
        userId: Long,
        @Positive(message = "코스 ID는 양수여야 합니다.")
        @Schema(description = "상세조회 하고자 하는 코스의 ID", example = "1")
        id: Long,
    ): CourseDetailResponse

    @Operation(
        summary = "코스 정보 수정",
        description = "본인이 올린 코스의 정보를 수정합니다. 수정할 데이터만 값을 보내면 되며 데이터를 안보내면 기존값 유지",
        responses = [
            ApiResponse(responseCode = "204", description = "코스 정보 수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = """
        잘못된 요청:
        • 코스 ID가 양수가 아님  
        • 코스 설명이 1자 미만이거나 500자를 초과함
        • 코스명이 1자 미만이거나 30자를 초과함
        • 파일 사이즈가 1MB를 초과함
      """,
                content = arrayOf(Content()),
            ),
            ApiResponse(responseCode = "403", description = "유저가 생성한 코스가 아님", content = arrayOf(Content())),
            ApiResponse(responseCode = "404", description = "ID에 해당하는 코스가 존재하지 않음", content = arrayOf(Content())),
        ],
    )
    fun updateCourse(
        userId: Long,
        @Positive(message = "코스 ID는 양수여야 합니다.")
        @Schema(description = "수정하고자 하는 코스의 ID", example = "1")
        id: Long,
        request: CourseUpdateRequest,
        @MaxFileSize
        @Schema(description = "코스 대표 이미지", required = false)
        courseImage: MultipartFile?,
    )

    @Operation(
        summary = "코스 좋아요 토글",
        description = "코스의 좋아요를 토글합니다. 이미 좋아요를 누른 경우 좋아요가 취소되고, 좋아요를 누르지 않은 경우 좋아요가 추가됩니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "좋아요 토글 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 - 코스 ID가 양수가 아님",
                content = arrayOf(Content()),
            ),
            ApiResponse(
                responseCode = "404",
                description = "ID에 해당하는 코스가 존재하지 않음",
                content = arrayOf(Content()),
            ),
        ],
    )
    fun courseLikeToggle(
        userId: Long,
        @Positive(message = "코스 ID는 양수여야 합니다.")
        @Schema(description = "좋아요 토글하고자 하는 코스의 ID", example = "1")
        id: Long,
    ): CourseLikeResponse
}
