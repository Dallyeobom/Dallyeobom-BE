package kr.dallyeobom.controller.courseCompletionHistory

import kr.dallyeobom.controller.courseCompletionHistory.request.CourseCompletionCreateRequest
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionCreateResponse
import kr.dallyeobom.entity.User
import kr.dallyeobom.service.CourseCompletionHistoryService
import kr.dallyeobom.util.LoginUser
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
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
        @LoginUser
        user: User,
        @RequestPart
        request: CourseCompletionCreateRequest,
        @RequestPart(required = false)
        courseImage: MultipartFile?,
    ): CourseCompletionCreateResponse =
        CourseCompletionCreateResponse.from(courseCompletionHistoryService.createCourseCompletionHistory(user, request, courseImage))
}
