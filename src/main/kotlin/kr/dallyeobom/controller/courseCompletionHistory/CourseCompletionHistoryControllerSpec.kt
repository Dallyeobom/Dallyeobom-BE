package kr.dallyeobom.controller.courseCompletionHistory

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dallyeobom.config.swagger.SwaggerTag
import kr.dallyeobom.controller.courseCompletionHistory.request.CourseCompletionCreateRequest
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionCreateResponse
import kr.dallyeobom.entity.User
import kr.dallyeobom.util.validator.MaxFileSize
import org.springframework.validation.annotation.Validated
import org.springframework.web.multipart.MultipartFile

@Tag(name = SwaggerTag.COURSE_COMPLETION_HISTORY)
interface CourseCompletionHistoryControllerSpec {
    @Operation(
        summary = "코스 완주 기록 생성",
        description = "코스 완주 기록을 생성합니다.",
        responses = [
            ApiResponse(responseCode = "201", description = "코스 완주 기록 생성 성공"),
            ApiResponse(
                responseCode = "400",
                description = """
        잘못된 요청:
        • 코스 ID가 양수가 아님  
        • 리뷰의 길이가 1자 미만이거나 300자를 초과함
        • 소요시간이 양수가 아님
        • 경로가 비어있음
        • 경로의 위도 또는 경도가 범위를 벗어남
        • 코스 공개 설정이 잘못됨
        • 코스 ID가 null이고 공개 설정이 PRIVATE인대 코스 생성 정보를 넣은경우
        • 공개 설정이 PUBLIC인데 생성 정보가 없는경우
        • 코스 설명이 1자 미만이거나 500자를 초과함
        • 코스명이 1자 미만이거나 30자를 초과함
        • 파일 사이즈가 1MB를 초과함
      """,
                content = arrayOf(Content()),
            ),
            ApiResponse(responseCode = "404", description = "존재하지 않는 코스 ID를 입력한 경우", content = arrayOf(Content())),
        ],
    )
    fun createCourseCompletionHistory(
        user: User,
        @Validated request: CourseCompletionCreateRequest,
        @MaxFileSize
        courseImage: MultipartFile?,
    ): CourseCompletionCreateResponse
}
