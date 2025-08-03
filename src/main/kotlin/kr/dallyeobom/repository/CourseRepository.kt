package kr.dallyeobom.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import kr.dallyeobom.controller.common.request.SliceRequest
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseCompletionHistory
import kr.dallyeobom.entity.User
import kr.dallyeobom.util.getAll
import kr.dallyeobom.util.getSlice
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository :
    JpaRepository<Course, Long>,
    CustomCourseRepository

interface CustomCourseRepository {
    fun findNearByCourseByLocation(
        longitude: Double,
        latitude: Double,
        radius: Int,
        maxCount: Int,
    ): List<Course>

    fun findUserCompletedCourseIds(
        user: User,
        sliceRequest: SliceRequest,
    ): Slice<Long>
}

class CustomCourseRepositoryImpl(
    private val kotlinJdslJpqlExecutor: KotlinJdslJpqlExecutor,
) : CustomCourseRepository {
    override fun findNearByCourseByLocation(
        longitude: Double,
        latitude: Double,
        radius: Int,
        maxCount: Int,
    ): List<Course> =
        kotlinJdslJpqlExecutor.getAll(maxCount) {
            val entity = entity(Course::class, "c1")
            selectDistinct(entity)
                .from(
                    entity,
                ).whereAnd(
                    customExpression(
                        String::class,
                        "SDO_NN({0}, SDO_GEOMETRY(2001, 4326, SDO_POINT_TYPE({1}, {2}, NULL), NULL, NULL), 'distance=' || {3} || ' unit=meter', 1)",
                        entity(Course::startPoint),
                        latitude,
                        longitude,
                        radius,
                    ).eq("TRUE"),
                    entity(Course::deletedDateTime).isNull(),
                ).orderBy(
                    // 가장 많이 완주된 코스가 먼저 나오도록
                    select(count(CourseCompletionHistory::id))
                        .from(entity(CourseCompletionHistory::class))
                        .where(path(CourseCompletionHistory::course).eq(entity))
                        .asSubquery()
                        .desc(),
                )
        }

    override fun findUserCompletedCourseIds(
        user: User,
        sliceRequest: SliceRequest,
    ): Slice<Long> =
        kotlinJdslJpqlExecutor.getSlice(Pageable.ofSize(sliceRequest.size)) {
            selectDistinct(path(Course::id))
                .from(
                    entity(CourseCompletionHistory::class),
                    join(CourseCompletionHistory::course),
                ).whereAnd(
                    path(CourseCompletionHistory::user).eq(user),
                    path(Course::deletedDateTime).isNull(),
                    sliceRequest.lastId?.let { path(Course::id).lt(it) },
                ).orderBy(path(Course::id).desc())
        }
}
