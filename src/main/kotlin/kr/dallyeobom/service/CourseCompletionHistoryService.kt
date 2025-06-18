package kr.dallyeobom.service

import kr.dallyeobom.controller.courseCompletionHistory.request.CourseCompletionCreateRequest
import kr.dallyeobom.dto.CourseCreateDto
import kr.dallyeobom.entity.CourseCompletionHistory
import kr.dallyeobom.entity.CourseCreatorType
import kr.dallyeobom.entity.CourseVisibility
import kr.dallyeobom.entity.User
import kr.dallyeobom.exception.CourseNotFoundException
import kr.dallyeobom.repository.CourseCompletionHistoryRepository
import kr.dallyeobom.repository.CourseRepository
import kr.dallyeobom.repository.ObjectStorageRepository
import kr.dallyeobom.util.CourseCreteUtil
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
    private val courseCreteUtil: CourseCreteUtil,
    private val objectStorageRepository: ObjectStorageRepository,
) {
    @Transactional
    fun createCourseCompletionHistory(
        user: User,
        request: CourseCompletionCreateRequest,
        courseImage: MultipartFile?,
    ): CourseCompletionHistory {
        val course =
            if (request.courseId != null) {
                courseRepository.findById(request.courseId).getOrNull() ?: throw CourseNotFoundException()
            } else if (request.courseVisibility != CourseVisibility.PRIVATE) {
                requireNotNull(request.courseCreateInfo) { "코스 생성 정보가 필요합니다." }
                requireNotNull(request.courseVisibility) { "코스 공개 설정 정보가 필요합니다." }
                courseCreteUtil.saveCourse(
                    CourseCreateDto(
                        request.courseCreateInfo.name,
                        request.courseCreateInfo.description,
                        request.courseCreateInfo.courseLevel,
                        courseImage?.let {
                            requireNotNull(courseImage.originalFilename) { "코스 이미지의 원본 파일명이 필요합니다." }
                            objectStorageRepository.upload(
                                ObjectStorageRepository.COURSE_IMAGE_PATH,
                                ObjectStorageRepository.generateFileName(
                                    FilenameUtils
                                        .getExtension(courseImage.originalFilename)
                                        .lowercase(Locale.getDefault()),
                                ),
                                courseImage.inputStream,
                            )
                        },
                        CourseCreatorType.USER,
                        creatorId = user.id,
                        request.latLngPath,
                        visibility = request.courseVisibility,
                    ),
                )
            } else {
                null
            }

        return courseCompletionHistoryRepository.save(
            CourseCompletionHistory(
                user = user,
                course = course,
                review = request.review,
                interval = Duration.ofSeconds(request.interval),
                path = courseCreteUtil.latLngToLineString(request.latLngPath),
            ),
        )
    }
}
