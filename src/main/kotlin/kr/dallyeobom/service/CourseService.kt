package kr.dallyeobom.service

import com.google.maps.model.LatLng
import io.jenetics.jpx.GPX
import kr.dallyeobom.client.TourApiClient
import kr.dallyeobom.controller.common.request.SliceRequest
import kr.dallyeobom.controller.common.response.SliceResponse
import kr.dallyeobom.controller.course.request.CourseUpdateRequest
import kr.dallyeobom.controller.course.request.NearByCourseSearchRequest
import kr.dallyeobom.controller.course.response.CourseDetailResponse
import kr.dallyeobom.controller.course.response.CourseLikeResponse
import kr.dallyeobom.controller.course.response.CourseRankResponse
import kr.dallyeobom.controller.course.response.CourseReviewResponse
import kr.dallyeobom.controller.course.response.CourseSearchResponse
import kr.dallyeobom.controller.course.response.NearByUserRunningCourseResponse
import kr.dallyeobom.controller.course.response.UserLikedCourseResponse
import kr.dallyeobom.dto.CourseCreateDto
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseCreatorType
import kr.dallyeobom.entity.CourseLevel
import kr.dallyeobom.entity.CourseLikeHistory
import kr.dallyeobom.entity.CourseVisibility
import kr.dallyeobom.entity.UserRunningCourse
import kr.dallyeobom.exception.BaseException
import kr.dallyeobom.exception.CourseNotFoundException
import kr.dallyeobom.exception.ErrorCode
import kr.dallyeobom.exception.NotCourseCreatorException
import kr.dallyeobom.exception.UserNotFoundException
import kr.dallyeobom.repository.CourseCompletionHistoryRepository
import kr.dallyeobom.repository.CourseCompletionImageRepository
import kr.dallyeobom.repository.CourseLikeHistoryRepository
import kr.dallyeobom.repository.CourseRepository
import kr.dallyeobom.repository.ObjectStorageRepository
import kr.dallyeobom.repository.UserRepository
import kr.dallyeobom.repository.UserRunningCourseRepository
import kr.dallyeobom.util.CourseCreateUtil
import kr.dallyeobom.util.lock.RedisLock
import org.springframework.data.domain.SliceImpl
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
    private val userRunningCourseRepository: UserRunningCourseRepository,
    private val courseCompletionImageRepository: CourseCompletionImageRepository,
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

    @Transactional(readOnly = true)
    fun searchNearByLocation(
        userId: Long,
        request: NearByCourseSearchRequest,
    ): List<CourseSearchResponse> {
        val courses =
            courseRepository
                .findNearByCourseByLocation(
                    request.longitude,
                    request.latitude,
                    request.radius,
                    request.maxCount,
                )
        val likedCourseIds =
            courseLikeHistoryRepository
                .findByUserIdAndCourseIn(
                    userId,
                    courses,
                ).map { it.course.id }
                .toSet()
        return courses.map {
            CourseSearchResponse.from(
                it,
                objectStorageRepository.getDownloadUrl(it.overviewImage),
                it.id in likedCourseIds,
            )
        }
    }

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

    @Transactional(readOnly = true)
    fun getCourseImages(
        id: Long,
        sliceRequest: SliceRequest,
    ): SliceResponse<String> {
        val course = courseRepository.findById(id).getOrNull() ?: throw CourseNotFoundException()
        val images = courseCompletionImageRepository.findSliceByCourse(course, sliceRequest)
        return SliceResponse.from(images.map { objectStorageRepository.getDownloadUrl(it.image) }, images.lastOrNull()?.id)
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
        return rankings.map { ranking ->
            CourseRankResponse.from(
                ranking,
                ranking.user.profileImage?.let { image -> objectStorageRepository.getDownloadUrl(image) },
            )
        }
    }

    @RedisLock(
        prefix = "reportRunningCourse",
        key = "#userId",
        waitTime = 5,
        leaseTime = 3,
    ) // 한 유저에 대해 동시에 하나의 요청만 처리하도록 락을 건다
    @Transactional
    fun reportRunningCourse(
        userId: Long,
        id: Long,
    ) {
        val course = courseRepository.findById(id).getOrNull()?.takeIf { it.deletedDateTime == null } ?: throw CourseNotFoundException()
        val user = userRepository.findById(userId).get()
        val runningCourse = userRunningCourseRepository.findByUser(user)
        var isRefreshed = false
        if (runningCourse != null) {
            if (runningCourse.course.id == id) {
                // 현재 유저가 달리고 있는 코스가 이미 등록되어 있다면, 해당 코스를 수정시간을 갱신한다
                runningCourse.refreshModifiedDateTime()
                isRefreshed = true
            } else {
                // 현재 유저가 달리고 있는 코스가 다른 코스라면, 해당 코스를 삭제한다
                userRunningCourseRepository.delete(runningCourse)
            }
        }
        if (!isRefreshed) {
            // 현재 유저가 달리고 있는 코스가 등록되어 있지 않다면, 새로 등록한다
            userRunningCourseRepository.save(UserRunningCourse(course, user))
        }
    }

    @Transactional(readOnly = true)
    fun getNearByUserRunningCourse(
        userId: Long,
        request: NearByCourseSearchRequest,
    ): List<NearByUserRunningCourseResponse> {
        val user = userRepository.findById(userId).get()
        val userRunningCourses =
            userRunningCourseRepository.getNearByUserRunningCourse(
                user,
                request.longitude,
                request.latitude,
                request.radius,
                request.maxCount,
            )
        return userRunningCourses.map {
            NearByUserRunningCourseResponse.from(
                it,
                it.course.image?.let { image -> objectStorageRepository.getDownloadUrl(image) },
                it.user.profileImage?.let { image -> objectStorageRepository.getDownloadUrl(image) },
            )
        }
    }

    @Transactional
    fun deleteRunningCourse(userId: Long) {
        userRunningCourseRepository.deleteByUserId(userId)
    }

    @Transactional(readOnly = true)
    fun getUserLikeCourses(
        userId: Long,
        id: Long,
        sliceRequest: SliceRequest,
    ): SliceResponse<UserLikedCourseResponse> {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
        val likedCourses = courseLikeHistoryRepository.findSliceByUser(user, sliceRequest)

        // 조회하고자 하는 유저의 좋아요 여부와 요청을 보낸 유저의 좋아요 여부를 구분하기 위해 로그인한 유저의 좋아요 기록을 따로 필요한것만 가져온다.
        val loginUserLikedCourseIds =
            courseLikeHistoryRepository
                .findByUserIdAndCourseIn(
                    userId,
                    likedCourses.content.mapNotNull { it.course },
                ).map { it.course.id }
                .toSet()
        return SliceResponse.from(
            likedCourses.map { likedCourse ->
                UserLikedCourseResponse.from(
                    likedCourse,
                    objectStorageRepository.getDownloadUrl(likedCourse.course.overviewImage),
                    likedCourse.course.id in loginUserLikedCourseIds,
                )
            },
            likedCourses.lastOrNull()?.id,
        )
    }

    @Transactional(readOnly = true)
    fun getUserCompletedCourses(
        userId: Long,
        id: Long,
        sliceRequest: SliceRequest,
    ): SliceResponse<CourseSearchResponse> {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
        // completedCourseIds와 completedCourses를 쿼리 한번으로 가져오려면 비효율적인 쿼리로 나와서 2번 쿼리로 나눠서 처리한다
        val completedCourseIds = courseRepository.findUserCompletedCourseIds(user, sliceRequest)
        val completedCourses =
            courseRepository.findAllById(completedCourseIds.content).sortedByDescending { it.id }

        val likedCourseIds =
            courseLikeHistoryRepository
                .findByUserIdAndCourseIn(
                    userId,
                    completedCourses,
                ).map { it.course.id }
                .toSet()
        return SliceResponse.from(
            SliceImpl(
                completedCourses.map { course ->
                    CourseSearchResponse.from(
                        course,
                        objectStorageRepository.getDownloadUrl(course.overviewImage),
                        course.id in likedCourseIds,
                    )
                },
                completedCourseIds.pageable,
                completedCourseIds.hasNext(),
            ),
            completedCourses.lastOrNull()?.id,
        )
    }

    @Transactional(readOnly = true)
    fun getCourseReviews(
        id: Long,
        sliceRequest: SliceRequest,
    ): SliceResponse<CourseReviewResponse> {
        val course = courseRepository.findById(id).getOrNull() ?: throw CourseNotFoundException()
        val completionHistories = courseCompletionHistoryRepository.findSliceByCourse(course, sliceRequest)
        val imageMap = courseCompletionImageRepository.findAllByCompletionIn(completionHistories.content).groupBy { it.completion.id }
        val reviewResponses =
            completionHistories.map { completionHistory ->
                CourseReviewResponse.from(
                    completionHistory,
                    completionHistory.user.profileImage?.let { image -> objectStorageRepository.getDownloadUrl(image) },
                    imageMap[completionHistory.id]?.map { objectStorageRepository.getDownloadUrl(it.image) },
                )
            }
        return SliceResponse.from(reviewResponses, completionHistories.lastOrNull()?.id)
    }
}
