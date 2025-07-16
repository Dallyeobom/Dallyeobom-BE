package kr.dallyeobom.service

import com.google.maps.model.LatLng
import kr.dallyeobom.controller.common.request.SliceRequest
import kr.dallyeobom.controller.common.response.SliceResponse
import kr.dallyeobom.controller.courseCompletionHistory.request.CourseCompletionCreateRequest
import kr.dallyeobom.controller.courseCompletionHistory.request.CourseCompletionUpdateRequest
import kr.dallyeobom.controller.courseCompletionHistory.request.CourseCreateRequest
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionCreateResponse
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionHistoryDetailResponse
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionHistoryResponse
import kr.dallyeobom.dto.CourseCreateDto
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseCompletionHistory
import kr.dallyeobom.entity.CourseCompletionImage
import kr.dallyeobom.entity.CourseCreatorType
import kr.dallyeobom.entity.CourseVisibility
import kr.dallyeobom.exception.AlreadyCreatedCourseException
import kr.dallyeobom.exception.CourseCompletionHistoryNotFoundException
import kr.dallyeobom.exception.CourseCompletionImageNotFoundException
import kr.dallyeobom.exception.CourseNotFoundException
import kr.dallyeobom.exception.InvalidCourseCompletionImageCountException
import kr.dallyeobom.exception.NotCourseCompletionHistoryCreatorException
import kr.dallyeobom.exception.UserNotFoundException
import kr.dallyeobom.repository.CourseCompletionHistoryRepository
import kr.dallyeobom.repository.CourseCompletionImageRepository
import kr.dallyeobom.repository.CourseRepository
import kr.dallyeobom.repository.ObjectStorageRepository
import kr.dallyeobom.repository.UserRepository
import kr.dallyeobom.util.CourseCreateUtil
import kr.dallyeobom.util.CourseLengthUtil
import kr.dallyeobom.util.lock.RedisLock
import kr.dallyeobom.util.requireNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Duration
import kotlin.jvm.optionals.getOrNull

@Service
class CourseCompletionHistoryService(
    private val courseRepository: CourseRepository,
    private val courseCompletionHistoryRepository: CourseCompletionHistoryRepository,
    private val courseCreateUtil: CourseCreateUtil,
    private val objectStorageRepository: ObjectStorageRepository,
    private val userRepository: UserRepository,
    private val courseCompletionImageRepository: CourseCompletionImageRepository,
) {
    @Transactional
    fun createCourseCompletionHistory(
        userId: Long,
        request: CourseCompletionCreateRequest,
        courseImage: MultipartFile?,
        completionImages: List<MultipartFile>,
    ): CourseCompletionCreateResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException(userId) }
        val courseCompletionHistory =
            courseCompletionHistoryRepository
                .save(
                    CourseCompletionHistory(
                        user = user,
                        course = getOrCreateCourseIfNeeded(userId, request, courseImage),
                        review = request.review,
                        interval = Duration.ofSeconds(request.interval),
                        path = courseCreateUtil.latLngToLineString(request.latLngPath),
                        length = CourseLengthUtil.calculateTotalDistance(request.latLngPath),
                    ),
                )

        saveCompletionImages(
            courseCompletionHistory,
            completionImages,
        )

        return CourseCompletionCreateResponse.from(courseCompletionHistory)
    }

    private fun getOrCreateCourseIfNeeded(
        userId: Long,
        request: CourseCompletionCreateRequest,
        courseImage: MultipartFile?,
    ): Course? =
        if (request.courseId != null) {
            requireNull(request.courseVisibility) { "코스 공개 설정 정보가 존재합니다" }
            requireNull(request.courseCreateInfo) { "코스 생성 정보가 존재합니다" }
            courseRepository.findById(request.courseId).getOrNull() ?: throw CourseNotFoundException()
        } else if (request.courseVisibility != CourseVisibility.PRIVATE) {
            requireNotNull(request.courseCreateInfo) { "코스 생성 정보가 필요합니다." }
            requireNotNull(request.courseVisibility) { "코스 공개 설정 정보가 필요합니다." }
            courseCreateUtil.saveCourse(
                CourseCreateDto(
                    request.courseCreateInfo.name,
                    request.courseCreateInfo.description,
                    request.courseCreateInfo.courseLevel,
                    courseImage?.let {
                        saveImage(ObjectStorageRepository.COURSE_IMAGE_PATH, courseImage)
                    },
                    CourseCreatorType.USER,
                    creatorId = userId,
                    request.latLngPath,
                    visibility = request.courseVisibility,
                ),
            )
        } else {
            requireNull(request.courseCreateInfo) { "비공개 코스 완주 기록 시 코스 생성 정보는 불필요합니다." }
            null
        }

    @Transactional(readOnly = true)
    fun getCourseCompletionHistoryDetail(
        userId: Long,
        id: Long,
    ): CourseCompletionHistoryDetailResponse {
        val courseCompletionHistory =
            courseCompletionHistoryRepository.findById(id).orElseThrow { CourseCompletionHistoryNotFoundException() }
        val completionImages = courseCompletionImageRepository.findAllByCompletion(courseCompletionHistory)
        val images =
            completionImages.map { image ->
                CourseCompletionHistoryDetailResponse.CourseCompletionImageResponse(
                    image.id,
                    objectStorageRepository.getDownloadUrl(image.image),
                )
            }

        return CourseCompletionHistoryDetailResponse.from(userId, courseCompletionHistory, images)
    }

    @Transactional(readOnly = true)
    fun getCourseCompletionHistoryListByUserId(
        userId: Long,
        sliceRequest: SliceRequest,
    ): SliceResponse<CourseCompletionHistoryResponse> {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException(userId) }
        val courseCompletionHistories =
            courseCompletionHistoryRepository.findSliceByUser(user, sliceRequest)
        val completionImageMap =
            courseCompletionImageRepository
                .findAllByCompletionIn(
                    courseCompletionHistories.content,
                ).associateBy { it.completion.id }

        return SliceResponse.from(
            courseCompletionHistories.map { courseCompletionHistory ->
                CourseCompletionHistoryResponse.from(
                    courseCompletionHistory,
                    objectStorageRepository.getDownloadUrl(
                        completionImageMap[courseCompletionHistory.id]!!.image,
                    ),
                )
            },
            courseCompletionHistories.lastOrNull()?.id,
        )
    }

    @Transactional
    fun createCourseFromCompletionHistory(
        userId: Long,
        id: Long,
        request: CourseCreateRequest,
        courseImage: MultipartFile?,
    ) {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException(userId) }
        val courseCompletionHistory =
            courseCompletionHistoryRepository.findById(id).orElseThrow { CourseCompletionHistoryNotFoundException() }

        if (courseCompletionHistory.user.id != user.id) {
            throw NotCourseCompletionHistoryCreatorException()
        }

        if (courseCompletionHistory.course != null) {
            throw AlreadyCreatedCourseException()
        }

        val course =
            courseCreateUtil.saveCourse(
                CourseCreateDto(
                    request.name,
                    request.description,
                    request.courseLevel,
                    courseImage?.let {
                        saveImage(ObjectStorageRepository.COURSE_IMAGE_PATH, courseImage)
                    },
                    CourseCreatorType.USER,
                    creatorId = userId,
                    path = courseCompletionHistory.path.coordinates.map { LatLng(it.x, it.y) },
                    visibility = CourseVisibility.PUBLIC,
                ),
            )

        courseCompletionHistory.course = course
    }

    private fun saveImage(
        path: String,
        imageFile: MultipartFile,
    ): String {
        requireNotNull(imageFile.originalFilename) { "코스 이미지의 원본 파일명이 필요합니다." }
        return objectStorageRepository.upload(
            path,
            imageFile,
        )
    }

    @Transactional
    fun deleteCourseCompletionHistory(
        userId: Long,
        id: Long,
    ) {
        val courseCompletionHistory =
            courseCompletionHistoryRepository.findById(id).orElseThrow { CourseCompletionHistoryNotFoundException() }

        if (courseCompletionHistory.user.id != userId) {
            throw NotCourseCompletionHistoryCreatorException()
        }

        val images = courseCompletionImageRepository.findAllByCompletion(courseCompletionHistory)

        courseCompletionImageRepository.deleteAll(images)
        courseCompletionHistoryRepository.delete(courseCompletionHistory)
        val course = courseCompletionHistory.course
        if (course != null && course.creatorId == userId && course.deletedDateTime == null) {
            courseRepository.deleteById(course.id)
        }
        images.forEach { image ->
            objectStorageRepository.delete(image.image)
        }
    }

    @RedisLock(
        prefix = "updateCourseCompletionHistory",
        key = "#id",
        waitTime = 10,
        leaseTime = 8,
    )
    @Transactional
    fun updateCourseCompletionHistory(
        userId: Long,
        id: Long,
        request: CourseCompletionUpdateRequest,
        completionImages: List<MultipartFile>?,
    ) {
        val courseCompletionHistory =
            courseCompletionHistoryRepository.findById(id).orElseThrow { CourseCompletionHistoryNotFoundException() }

        if (courseCompletionHistory.user.id != userId) {
            throw NotCourseCompletionHistoryCreatorException()
        }

        val existingImages = courseCompletionImageRepository.findAllByCompletion(courseCompletionHistory)
        val imageCount = existingImages.size + (completionImages?.size ?: 0) - (request.deleteImageIds?.size ?: 0)
        if (imageCount > 3) {
            throw InvalidCourseCompletionImageCountException("인증샷은 최대 3개까지 업로드할 수 있습니다.")
        } else if (imageCount == 0) {
            throw InvalidCourseCompletionImageCountException("인증샷은 최소 1개 이상이어야 합니다.")
        }
        request.review?.let { courseCompletionHistory.review = it }

        if (completionImages != null) {
            saveCompletionImages(courseCompletionHistory, completionImages)
        }

        if (!request.deleteImageIds.isNullOrEmpty()) {
            request.deleteImageIds.forEach { imageId ->
                val image =
                    existingImages.find { it.id == imageId }
                        ?: throw CourseCompletionImageNotFoundException(imageId)
                courseCompletionImageRepository.delete(image)
            }
            // ObjectStorage에서 이미지 삭제를 했는데 불의의 사태로 트랜잭션이 롤백되면 ObjectStorage와 DB간 불일치가 발생할 수 있다
            // 따라서, 트랜잭션이 롤백될 위험이 없을때 이미지를 삭제하기 위해 별도의 반복문으로 분리한다
            request.deleteImageIds.forEach { imageId ->
                val image =
                    existingImages.find { it.id == imageId }!! // 이미 존재하는 이미지이므로 null이 아님
                objectStorageRepository.delete(image.image)
            }
        }
    }

    private fun saveCompletionImages(
        courseCompletionHistory: CourseCompletionHistory,
        completionImages: List<MultipartFile>,
    ) {
        courseCompletionImageRepository.saveAll(
            completionImages.map { completionImage ->
                CourseCompletionImage(
                    user = courseCompletionHistory.user,
                    completion = courseCompletionHistory,
                    image = saveImage(ObjectStorageRepository.COMPLETION_IMAGE_PATH, completionImage),
                )
            },
        )
    }
}
