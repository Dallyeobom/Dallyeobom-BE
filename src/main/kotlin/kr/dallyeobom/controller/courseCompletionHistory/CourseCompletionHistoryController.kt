package kr.dallyeobom.controller.courseCompletionHistory

import jakarta.validation.constraints.Positive
import kr.dallyeobom.controller.common.request.SliceRequest
import kr.dallyeobom.controller.common.response.SliceResponse
import kr.dallyeobom.controller.courseCompletionHistory.request.CourseCompletionCreateRequest
import kr.dallyeobom.controller.courseCompletionHistory.request.CourseCreateRequest
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionCreateResponse
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionHistoryDetailResponse
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionHistoryResponse
import kr.dallyeobom.service.CourseCompletionHistoryService
import kr.dallyeobom.util.LoginUserId
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/course-completion-history")
class CourseCompletionHistoryController(
    private val courseCompletionHistoryService: CourseCompletionHistoryService,
) : CourseCompletionHistoryControllerSpec {
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = [MULTIPART_FORM_DATA_VALUE])
    override fun createCourseCompletionHistory(
        @LoginUserId
        userId: Long,
        @RequestPart
        request: CourseCompletionCreateRequest,
        @RequestPart(required = false)
        courseImage: MultipartFile?,
        @RequestPart
        completionImages: List<MultipartFile>,
    ): CourseCompletionCreateResponse =
        courseCompletionHistoryService.createCourseCompletionHistory(userId, request, courseImage, completionImages)

    @GetMapping("/{id}")
    override fun getCourseCompletionHistoryDetail(
        @LoginUserId
        userId: Long,
        @PathVariable
        @Positive(message = "코스 완주 기록 ID는 양수여야 합니다.")
        id: Long,
    ): CourseCompletionHistoryDetailResponse = courseCompletionHistoryService.getCourseCompletionHistoryDetail(userId, id)

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/create-course", consumes = [MULTIPART_FORM_DATA_VALUE])
    override fun createCourseFromCompletionHistory(
        @LoginUserId
        userId: Long,
        @PathVariable
        id: Long,
        @RequestPart
        request: CourseCreateRequest,
        @RequestPart(required = false)
        courseImage: MultipartFile?,
    ) = courseCompletionHistoryService.createCourseFromCompletionHistory(userId, id, request, courseImage)

    @GetMapping("/user/{userId}")
    override fun getCourseCompletionHistoryListByUserId(
        @PathVariable
        userId: Long,
        sliceRequest: SliceRequest,
    ): SliceResponse<CourseCompletionHistoryResponse> =
        courseCompletionHistoryService.getCourseCompletionHistoryListByUserId(userId, sliceRequest)

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}/delete")
    override fun deleteCourseCompletionHistory(
        @LoginUserId
        userId: Long,
        @PathVariable id: Long,
    ) = courseCompletionHistoryService.deleteCourseCompletionHistory(userId, id)
}
