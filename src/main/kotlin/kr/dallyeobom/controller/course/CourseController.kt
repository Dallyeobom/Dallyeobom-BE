package kr.dallyeobom.controller.course

import kr.dallyeobom.controller.course.request.NearByCourseSearchRequest
import kr.dallyeobom.controller.course.response.CourseDetailResponse
import kr.dallyeobom.controller.course.response.NearByCourseSearchResponse
import kr.dallyeobom.service.CourseService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

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
    override fun searchNearByCourse(request: NearByCourseSearchRequest): List<NearByCourseSearchResponse> =
        courseService.searchNearByCourse(request)

    @GetMapping("/{id}")
    override fun getCourseDetail(
        @PathVariable id: Long,
    ): CourseDetailResponse = courseService.getCourseDetail(id)
}
