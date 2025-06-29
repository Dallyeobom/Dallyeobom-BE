package kr.dallyeobom.repository

import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseLikeHistory
import kr.dallyeobom.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface CourseLikeHistoryRepository : JpaRepository<CourseLikeHistory, Long> {
    fun existsByCourseAndUser(
        course: Course,
        user: User,
    ): Boolean

    fun deleteByCourseAndUser(
        course: Course,
        user: User,
    ): Int

    fun countByCourse(course: Course): Int
}
