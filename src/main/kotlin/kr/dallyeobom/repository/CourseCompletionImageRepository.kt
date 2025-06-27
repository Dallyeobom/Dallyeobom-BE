package kr.dallyeobom.repository

import kr.dallyeobom.entity.CourseCompletionHistory
import kr.dallyeobom.entity.CourseCompletionImage
import org.springframework.data.jpa.repository.JpaRepository

interface CourseCompletionImageRepository : JpaRepository<CourseCompletionImage, Long> {
    fun findAllByCompletion(completion: CourseCompletionHistory): List<CourseCompletionImage>

    fun findAllByCompletionIn(completions: List<CourseCompletionHistory>): List<CourseCompletionImage>
}
