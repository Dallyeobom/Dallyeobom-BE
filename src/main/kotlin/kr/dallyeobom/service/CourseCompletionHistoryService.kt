package kr.dallyeobom.service

import kr.dallyeobom.controller.common.request.SliceRequest
import kr.dallyeobom.controller.common.response.SliceResponse
import kr.dallyeobom.controller.courseCompletionHistory.request.CourseCompletionCreateRequest
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionCreateResponse
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionHistoryDetailResponse
import kr.dallyeobom.controller.courseCompletionHistory.response.CourseCompletionHistoryResponse
import kr.dallyeobom.dto.CourseCreateDto
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseCompletionHistory
import kr.dallyeobom.entity.CourseCompletionImage
import kr.dallyeobom.entity.CourseCreatorType
import kr.dallyeobom.entity.CourseVisibility
import kr.dallyeobom.exception.CourseCompletionHistoryNotFoundException
import kr.dallyeobom.exception.CourseNotFoundException
import kr.dallyeobom.exception.UserNotFoundException
import kr.dallyeobom.repository.CourseCompletionHistoryRepository
import kr.dallyeobom.repository.CourseCompletionImageRepository
import kr.dallyeobom.repository.CourseRepository
import kr.dallyeobom.repository.ObjectStorageRepository
import kr.dallyeobom.repository.UserRepository
import kr.dallyeobom.util.CourseCreateUtil
import kr.dallyeobom.util.CourseLengthUtil
import kr.dallyeobom.util.requireNull
import org.apache.commons.io.FilenameUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Duration
import java.util.Locale
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
        val user = userRepository.findById(userId).get() // 없을수가 없는 정보라 get() 사용
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

        courseCompletionImageRepository.saveAll(
            completionImages.map { courseCompletionImage ->
                CourseCompletionImage(
                    user = user,
                    completion = courseCompletionHistory,
                    image = saveImage(ObjectStorageRepository.COMPLETION_IMAGE_PATH, courseCompletionImage),
                )
            },
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
    fun getCourseCompletionHistoryDetail(id: Long): CourseCompletionHistoryDetailResponse {
        val courseCompletionHistory =
            courseCompletionHistoryRepository.findById(id).orElseThrow { CourseCompletionHistoryNotFoundException() }
        val completionImages = courseCompletionImageRepository.findAllByCompletion(courseCompletionHistory)
        val imageUrl = completionImages.map { image -> objectStorageRepository.getDownloadUrl(image.image) }

        return CourseCompletionHistoryDetailResponse.from(courseCompletionHistory, imageUrl)
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

    private fun saveImage(
        path: String,
        imageFile: MultipartFile,
    ): String {
        requireNotNull(imageFile.originalFilename) { "코스 이미지의 원본 파일명이 필요합니다." }
        return objectStorageRepository.upload(
            path,
            ObjectStorageRepository.generateFileName(
                FilenameUtils
                    .getExtension(imageFile.originalFilename)
                    .lowercase(Locale.getDefault()),
            ),
            imageFile.inputStream,
        )
    }
}
