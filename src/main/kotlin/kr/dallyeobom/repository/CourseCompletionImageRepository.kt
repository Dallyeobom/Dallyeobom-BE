package kr.dallyeobom.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import kr.dallyeobom.controller.common.request.SliceRequest
import kr.dallyeobom.dto.CourseImageDto
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseCompletionHistory
import kr.dallyeobom.entity.CourseCompletionImage
import kr.dallyeobom.util.getSlice
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CourseCompletionImageRepository :
    JpaRepository<CourseCompletionImage, Long>,
    CustomCourseCompletionImageRepository {
    fun findAllByCompletion(completion: CourseCompletionHistory): List<CourseCompletionImage>

    fun findAllByCompletionIn(completions: List<CourseCompletionHistory>): List<CourseCompletionImage>
    @Modifying(clearAutomatically = true)
    @Query(
        value = "DELETE FROM COURSE_COMPLETION_IMAGE CCI WHERE CCI.USER_ID=:userId", nativeQuery = true
    )
    fun deleteByUser(userId: Long)
}

interface CustomCourseCompletionImageRepository {
    fun findSliceByCourse(
        course: Course,
        sliceRequest: SliceRequest,
    ): Slice<CourseImageDto>
}

class CustomCourseCompletionImageRepositoryImpl(
    private val kotlinJdslJpqlExecutor: KotlinJdslJpqlExecutor,
) : CustomCourseCompletionImageRepository {
    override fun findSliceByCourse(
        course: Course,
        sliceRequest: SliceRequest,
    ) = kotlinJdslJpqlExecutor.getSlice(Pageable.ofSize(sliceRequest.size)) {
        val courseCompletionImageEntity = entity(CourseCompletionImage::class)
        selectNew(
            CourseImageDto::class,
            path(CourseCompletionImage::image),
            path(CourseCompletionImage::id),
        ).from(
            courseCompletionImageEntity,
            join(CourseCompletionImage::completion),
        ).whereAnd(
            path(CourseCompletionHistory::course).eq(course),
            sliceRequest.lastId?.let { lastId -> path(CourseCompletionImage::id).lt(lastId) },
        ).orderBy(
            path(CourseCompletionImage::id).desc(),
        )
    }
}
