package kr.dallyeobom.service

import com.google.maps.model.LatLng
import io.jenetics.jpx.GPX
import kr.dallyeobom.client.TourApiClient
import kr.dallyeobom.controller.course.request.NearByCourseSearchRequest
import kr.dallyeobom.controller.course.response.CourseDetailResponse
import kr.dallyeobom.controller.course.response.NearByCourseSearchResponse
import kr.dallyeobom.dto.CourseCreateDto
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseCreatorType
import kr.dallyeobom.entity.CourseLevel
import kr.dallyeobom.entity.CourseVisibility
import kr.dallyeobom.exception.BaseException
import kr.dallyeobom.exception.CourseNotFoundException
import kr.dallyeobom.exception.ErrorCode
import kr.dallyeobom.repository.CourseRepository
import kr.dallyeobom.repository.ObjectStorageRepository
import kr.dallyeobom.util.CourseCreateUtil
import org.springframework.stereotype.Service
import java.util.Locale
import kotlin.jvm.optionals.getOrNull

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val objectStorageRepository: ObjectStorageRepository,
    private val tourApiClient: TourApiClient,
    private val courseCreateUtil: CourseCreateUtil,
) {
    // 관광데이터 API를 통해 경로를 가져오고, DB를 채워넣는 메소드
    // 해당 메소드는 멱등성보장이 되지 않는다. 따라서 여러번 호출하면 중복된 코스가 생성된다.
    fun createCoursesWithTourApi(): List<Course> {
        val courses =
            tourApiClient.getCourseList()
                ?: throw BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "코스 리스트를 가져오는 데 실패했습니다. 관광데이터 API가 정상적으로 작동하는지 확인해주세요.")
        return courses.map { course ->
            val gpx = GPX.Reader.of(GPX.Reader.Mode.LENIENT).read(tourApiClient.getFileWithStream(course.gpxpath))
            val path =
                gpx.tracks
                    .flatMap { it.segments }
                    .flatMap { it.points }
                    .map { LatLng(it.latitude.toDegrees(), it.longitude.toDegrees()) }
            val image =
                tourApiClient.getLocationImageUrl(path.first())?.let { imageUrl ->
                    val fileName =
                        ObjectStorageRepository.generateFileName(
                            imageUrl.split(".").last().lowercase(Locale.getDefault()),
                        ) // 확장자 추출후 소문자로 통일
                    try {
                        tourApiClient.getFileWithStream(imageUrl)?.let { stream ->
                            objectStorageRepository.upload(
                                ObjectStorageRepository.COURSE_IMAGE_PATH,
                                fileName,
                                stream,
                            )
                        }
                    } catch (e: Exception) {
                        // 왜 인지 모르겠는데 해당 API가 실행 안되는 좌표들이 간혹 있다. 500에러 발생하면 재시도 해도 안됨
                        // 이 경우에는 null로 처리
                        null
                    }
                }
            courseCreateUtil.saveCourse(
                CourseCreateDto(
                    course.crsKorNm,
                    course.crsContents,
                    CourseLevel.entries[course.crsLevel.toInt() - 1],
                    image,
                    CourseCreatorType.SYSTEM,
                    null,
                    path,
                    CourseVisibility.PUBLIC,
                ),
            )
        }
    }

    fun searchNearByLocation(request: NearByCourseSearchRequest): List<NearByCourseSearchResponse> =
        courseRepository
            .findNearByCourseByLocation(
                request.longitude,
                request.latitude,
                request.radius,
                request.maxCount,
            ).map { NearByCourseSearchResponse.from(it, objectStorageRepository.getDownloadUrl(it.overviewImage)) }

    fun getCourseDetail(id: Long): CourseDetailResponse {
        val course = courseRepository.findById(id).getOrNull() ?: throw CourseNotFoundException()
        return CourseDetailResponse.from(
            course,
            course.image?.let { objectStorageRepository.getDownloadUrl(it) },
            objectStorageRepository.getDownloadUrl(course.overviewImage),
        )
    }
}
