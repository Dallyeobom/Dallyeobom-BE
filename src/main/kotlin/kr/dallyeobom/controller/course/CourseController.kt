package kr.dallyeobom.controller.course

import kr.dallyeobom.controller.course.request.CourseUpdateRequest
import kr.dallyeobom.controller.course.request.NearByCourseSearchRequest
import kr.dallyeobom.controller.course.response.CourseDetailResponse
import kr.dallyeobom.controller.course.response.CourseLikeResponse
import kr.dallyeobom.controller.course.response.NearByCourseSearchResponse
import kr.dallyeobom.service.CourseService
import kr.dallyeobom.util.LoginUserId
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/course")
class CourseController(
    private val courseService: CourseService,
) : CourseControllerSpec {
    // 해당 API는 관광데이터 API를 통해 코스를 생성하는 API로, 1번만 실행해서 DB에 해당 데이터들이 들어갔다면 더이상 쓸모가 없다
    // 그러나 DB를 날려버리고 다시 채울때가 있다면 그때는 주석 해제 후 사용할것
    // @PostMapping("/insert-tour-api-course")
    @ResponseStatus(CREATED)
    override fun insertTourApiCourse() {
        courseService.createCoursesWithTourApi()
    }

    @GetMapping("/nearby")
    override fun searchNearByLocation(request: NearByCourseSearchRequest): List<NearByCourseSearchResponse> =
        courseService.searchNearByLocation(request)

    @GetMapping("/{id}")
    override fun getCourseDetail(
        @LoginUserId
        userId: Long,
        @PathVariable
        id: Long,
    ): CourseDetailResponse = courseService.getCourseDetail(userId, id)

    @ResponseStatus(NO_CONTENT)
    @PatchMapping("/{id}", consumes = [MULTIPART_FORM_DATA_VALUE])
    override fun updateCourse(
        @LoginUserId
        userId: Long,
        @PathVariable
        id: Long,
        @RequestPart
        request: CourseUpdateRequest,
        @RequestPart(required = false)
        courseImage: MultipartFile?,
    ) = courseService.updateCourse(userId, id, request, courseImage)

    @PostMapping("/{id}/like")
    override fun courseLikeToggle(
        @LoginUserId
        userId: Long,
        @PathVariable id: Long,
    ): CourseLikeResponse = courseService.courseLikeToggle(userId, id)
}
