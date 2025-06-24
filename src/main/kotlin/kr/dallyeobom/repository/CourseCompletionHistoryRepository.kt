package kr.dallyeobom.repository

import kr.dallyeobom.dto.UserRank
import kr.dallyeobom.entity.CourseCompletionHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalDateTime

interface CourseCompletionHistoryRepository : JpaRepository<CourseCompletionHistory, Long> {
    @Query(
        """
        SELECT new kr.dallyeobom.dto.UserRank(
            c.user.id,
            c.user.nickname,
            SUM(c.length),
            COUNT(c)
        )
        FROM CourseCompletionHistory c
        WHERE c.createdDateTime BETWEEN :startDateTime AND :endDateTime
        GROUP BY c.user.id, c.user.nickname
        ORDER BY SUM(c.length) DESC
        LIMIT :limit
    """,
    )
    fun getDateRangeUserRankings(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime = LocalDate.now().atStartOfDay(),
        limit: Int = 100,
    ): List<UserRank>
}
