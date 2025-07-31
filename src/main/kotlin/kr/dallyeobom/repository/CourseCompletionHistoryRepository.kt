package kr.dallyeobom.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import kr.dallyeobom.controller.common.request.SliceRequest
import kr.dallyeobom.dto.CourseRankingInfo
import kr.dallyeobom.dto.UserRank
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseCompletionHistory
import kr.dallyeobom.entity.User
import kr.dallyeobom.util.getAll
import kr.dallyeobom.util.getSlice
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.LocalDateTime

interface CourseCompletionHistoryRepository :
    JpaRepository<CourseCompletionHistory, Long>,
    CustomCourseCompletionHistoryRepository

interface CustomCourseCompletionHistoryRepository {
    fun getDateRangeUserRankings(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime = LocalDate.now().atStartOfDay(),
        limit: Int = 100,
    ): List<UserRank>

    fun findSliceByUser(
        user: User,
        sliceRequest: SliceRequest,
    ): Slice<CourseCompletionHistory>

    fun findCourseUserRankings(
        course: Course,
        limit: Int,
    ): List<CourseRankingInfo>

    fun findSliceByCourse(
        course: Course,
        sliceRequest: SliceRequest,
    ): Slice<CourseCompletionHistory>
}

class CustomCourseCompletionHistoryRepositoryImpl(
    private val kotlinJdslJpqlExecutor: KotlinJdslJpqlExecutor,
) : CustomCourseCompletionHistoryRepository {
    override fun getDateRangeUserRankings(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        limit: Int,
    ) = kotlinJdslJpqlExecutor.getAll(limit) {
        val courseCompletionHistoryEntity = entity(CourseCompletionHistory::class)
        selectNew<UserRank>(
            path(User::id),
            path(User::nickname),
            path(User::profileImage),
            sum(CourseCompletionHistory::length),
            count(courseCompletionHistoryEntity),
        ).from(
            courseCompletionHistoryEntity,
            join(CourseCompletionHistory::user),
        ).where(
            path(CourseCompletionHistory::createdDateTime).between(startDateTime, endDateTime),
        ).groupBy(
            // Id를 기준으로 그룹을 잡아도 되지만 ANSI SQL 표준에 따라 select 절에 있는 컬럼을 모두 group by에 넣어야함
            path(User::id),
            path(User::nickname),
            path(User::profileImage),
        ).orderBy(
            sum(CourseCompletionHistory::length).desc(),
        )
    }

    override fun findSliceByUser(
        user: User,
        sliceRequest: SliceRequest,
    ) = kotlinJdslJpqlExecutor.getSlice(Pageable.ofSize(sliceRequest.size)) {
        val entity = entity(CourseCompletionHistory::class)
        select(entity)
            .from(entity)
            .whereAnd(
                path(CourseCompletionHistory::user).eq(user),
                sliceRequest.lastId?.let { path(CourseCompletionHistory::id).lt(it) },
            ).orderBy(path(CourseCompletionHistory::id).desc())
    }

    override fun findCourseUserRankings(
        course: Course,
        limit: Int,
    ): List<CourseRankingInfo> =
        kotlinJdslJpqlExecutor.getAll(limit) {
            val courseCompletionHistory = entity(CourseCompletionHistory::class)
            selectNew<CourseRankingInfo>(
                path(CourseCompletionHistory::user),
                path(CourseCompletionHistory::interval),
            ).from(courseCompletionHistory)
                .where(path(CourseCompletionHistory::course).eq(course))
                .orderBy(
                    path(CourseCompletionHistory::interval).asc(),
                    path(CourseCompletionHistory::id).asc(), // 동점자 처리: id로 정렬하여 순위를 유지
                )
        }

    override fun findSliceByCourse(
        course: Course,
        sliceRequest: SliceRequest,
    ): Slice<CourseCompletionHistory> =
        kotlinJdslJpqlExecutor.getSlice(Pageable.ofSize(sliceRequest.size)) {
            val entity = entity(CourseCompletionHistory::class)
            select(entity)
                .from(entity)
                .whereAnd(
                    path(CourseCompletionHistory::course).eq(course),
                    sliceRequest.lastId?.let { path(CourseCompletionHistory::id).lt(it) },
                ).orderBy(path(CourseCompletionHistory::id).desc())
        }
}
