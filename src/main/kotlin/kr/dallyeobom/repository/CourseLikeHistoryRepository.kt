package kr.dallyeobom.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import kr.dallyeobom.controller.common.request.SliceRequest
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseLikeHistory
import kr.dallyeobom.entity.User
import kr.dallyeobom.util.getSlice
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository

interface CourseLikeHistoryRepository :
    JpaRepository<CourseLikeHistory, Long>,
    CustomCourseLikeHistoryRepository {
    fun deleteByCourseAndUser(
        course: Course,
        user: User,
    ): Int

    fun countByCourse(course: Course): Int

    fun findByUserIdAndCourseIn(
        userId: Long,
        courses: List<Course>,
    ): List<CourseLikeHistory>

    fun deleteByUser(user: User)
}

interface CustomCourseLikeHistoryRepository {
    fun findSliceByUser(
        user: User,
        sliceRequest: SliceRequest,
    ): Slice<CourseLikeHistory>
}

class CustomCourseLikeHistoryRepositoryImpl(
    private val kotlinJdslJpqlExecutor: KotlinJdslJpqlExecutor,
) : CustomCourseLikeHistoryRepository {
    override fun findSliceByUser(
        user: User,
        sliceRequest: SliceRequest,
    ): Slice<CourseLikeHistory> =
        kotlinJdslJpqlExecutor.getSlice(Pageable.ofSize(sliceRequest.size)) {
            val courseLikeHistoryEntity = entity(CourseLikeHistory::class)
            select(
                courseLikeHistoryEntity,
            ).from(courseLikeHistoryEntity)
                .whereAnd(
                    path(CourseLikeHistory::user).eq(user),
                    sliceRequest.lastId?.let { lastId -> path(CourseLikeHistory::id).lt(lastId) },
                ).orderBy(
                    path(CourseLikeHistory::id).desc(),
                )
        }
}
