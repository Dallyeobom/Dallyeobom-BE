package kr.dallyeobom.controller.courseCompletionHistory

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import kr.dallyeobom.config.swagger.SwaggerTag
import kr.dallyeobom.controller.common.request.SliceRequest
import kr.dallyeobom.controller.common.response.SliceResponse
import kr.dallyeobom.controller.courseCompletionHistory.request.CourseCompletionCreateRequest
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionCreateResponse
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionHistoryDetailResponse
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionHistoryResponse
import kr.dallyeobom.util.validator.MaxFileSize
import org.springdoc.core.annotations.ParameterObject
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
        • 코스 인증샷이 1개 미만이거나 3개를 초과함
        • 파일 사이즈가 1MB를 초과함
      """,
                content = arrayOf(Content()),
            ),
            ApiResponse(responseCode = "404", description = "존재하지 않는 코스 ID를 입력한 경우", content = arrayOf(Content())),
        ],
    )
    fun createCourseCompletionHistory(
        userId: Long,
        @Validated request: CourseCompletionCreateRequest,
        @MaxFileSize
        @Schema(description = "코스 대표사진 - 코스 등록시에만 사용하며 없어도됨, 사진의 최대 크기는 1MB")
        courseImage: MultipartFile?,
        @MaxFileSize
        @Size(min = 1, max = 3, message = "인증샷은 최소 1개에서 최대 3개까지 업로드할 수 있습니다.")
        @Schema(description = "코스 완주 인증샷들 - 1 ~ 3개까지 업로드 가능, 각 사진의 최대 크기는 1MB")
        completionImages: List<MultipartFile>,
    ): CourseCompletionCreateResponse

    @Operation(
        summary = "코스 완주기록 상세 조회",
        description = "코스 완주기록 ID를 입력받아 해당 기록의 상세 정보를 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "코스 완주 기록 상세 정보"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 - 완주 기록 ID가 양수가 아님", content = arrayOf(Content())),
            ApiResponse(responseCode = "404", description = "ID에 해당하는 기록 존재하지 않음", content = arrayOf(Content())),
        ],
    )
    fun getCourseCompletionHistoryDetail(
        @Positive(message = "코스 완주 기록 ID는 양수여야 합니다.")
        @Schema(description = "상세조회 하고자 하는 완주 기록의 ID", example = "1")
        id: Long,
    ): CourseCompletionHistoryDetailResponse

    @Operation(
        summary = "특정 유저의 코스 완주 기록 리스트 조회",
        description = "특정 유저의 코스 완주 기록 리스트를 조회합니다. 서버 조회 성능을 위해 무한스크롤 방식으로 구현되었습니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "코스 완주 기록 정보 리스트"),
            ApiResponse(responseCode = "404", description = "ID에 해당하는 유저가 존재하지 않음", content = arrayOf(Content())),
        ],
    )
    fun getCourseCompletionHistoryListByUserId(
        @Positive(message = "유저 ID는 양수여야 합니다.")
        @Schema(description = "조회하고자 하는 유저의 ID", example = "1")
        userId: Long,
        @Validated
        @ParameterObject sliceRequest: SliceRequest,
    ): SliceResponse<CourseCompletionHistoryResponse>
}
