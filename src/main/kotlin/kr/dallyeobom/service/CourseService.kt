package kr.dallyeobom.service

import com.google.maps.model.LatLng
import io.jenetics.jpx.GPX
import kr.dallyeobom.client.TourApiClient
import kr.dallyeobom.controller.course.request.CourseUpdateRequest
import kr.dallyeobom.controller.course.request.NearByCourseSearchRequest
import kr.dallyeobom.controller.course.response.CourseDetailResponse
import kr.dallyeobom.controller.course.response.CourseLikeResponse
import kr.dallyeobom.controller.course.response.CourseRankResponse
import kr.dallyeobom.controller.course.response.NearByCourseSearchResponse
import kr.dallyeobom.dto.CourseCreateDto
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseCreatorType
import kr.dallyeobom.entity.CourseLevel
import kr.dallyeobom.entity.CourseLikeHistory
import kr.dallyeobom.entity.CourseVisibility
import kr.dallyeobom.exception.BaseException
import kr.dallyeobom.exception.CourseNotFoundException
import kr.dallyeobom.exception.ErrorCode
import kr.dallyeobom.exception.NotCourseCreatorException
import kr.dallyeobom.repository.CourseCompletionHistoryRepository
import kr.dallyeobom.repository.CourseLikeHistoryRepository
import kr.dallyeobom.repository.CourseRepository
import kr.dallyeobom.repository.ObjectStorageRepository
import kr.dallyeobom.repository.UserRepository
import kr.dallyeobom.util.CourseCreateUtil
import kr.dallyeobom.util.lock.RedisLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.Locale
import kotlin.jvm.optionals.getOrNull

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val objectStorageRepository: ObjectStorageRepository,
    private val tourApiClient: TourApiClient,
    private val courseCreateUtil: CourseCreateUtil,
    private val courseLikeHistoryRepository: CourseLikeHistoryRepository,
    private val userRepository: UserRepository,
    private val courseCompletionHistoryRepository: CourseCompletionHistoryRepository,
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

    fun getCourseDetail(
        userId: Long,
        id: Long,
    ): CourseDetailResponse {
        val course = courseRepository.findById(id).getOrNull() ?: throw CourseNotFoundException()
        return CourseDetailResponse.from(
            userId,
            course,
            course.image?.let { objectStorageRepository.getDownloadUrl(it) },
            objectStorageRepository.getDownloadUrl(course.overviewImage),
        )
    }

    @Transactional
    fun updateCourse(
        userId: Long,
        id: Long,
        request: CourseUpdateRequest,
        imageFile: MultipartFile?,
    ) {
        val course = courseRepository.findById(id).getOrNull() ?: throw CourseNotFoundException()
        if (course.creatorId != userId) {
            throw NotCourseCreatorException()
        }

        request.name?.let { course.name = it }
        request.description?.let { course.description = it }
        request.courseLevel?.let { course.courseLevel = it }
        request.courseVisibility?.let {
            when (it) {
                CourseVisibility.PUBLIC -> course.restore()
                CourseVisibility.PRIVATE -> course.delete()
            }
            course.visibility = it
        }

        if (imageFile != null) {
            course.image?.let { objectStorageRepository.delete(it) }
            course.image =
                objectStorageRepository.upload(
                    ObjectStorageRepository.COURSE_IMAGE_PATH,
                    imageFile,
                )
        }
    }

    @RedisLock(
        prefix = "toggleCourseLike",
        key = "#userId + ':' +#id",
        waitTime = 5,
        leaseTime = 3,
    )
    @Transactional
    fun toggleCourseLike(
        userId: Long,
        id: Long,
    ): CourseLikeResponse {
        val user = userRepository.findById(userId).get()
        val course = courseRepository.findById(id).getOrNull()?.takeIf { it.deletedDateTime == null } ?: throw CourseNotFoundException()
        val isAlreadyLiked = courseLikeHistoryRepository.deleteByCourseAndUser(course, user) > 0 // 삭제된 행이 있다면 이미 좋아요를 누른 상태
        if (!isAlreadyLiked) {
            courseLikeHistoryRepository.save(CourseLikeHistory(course, user))
        }
        return CourseLikeResponse(!isAlreadyLiked, courseLikeHistoryRepository.countByCourse(course))
    }

    @Transactional(readOnly = true)
    fun getCourseUserRank(
        id: Long,
        size: Int,
    ): List<CourseRankResponse> {
        val course = courseRepository.findById(id).orElseThrow { CourseNotFoundException() }
        val rankings =
            courseCompletionHistoryRepository.findCourseUserRankings(
                course,
                size,
            )
        return rankings.map(CourseRankResponse::from)
    }
}
