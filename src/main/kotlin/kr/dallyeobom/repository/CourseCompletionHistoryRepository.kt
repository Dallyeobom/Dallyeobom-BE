package kr.dallyeobom.repository

import kr.dallyeobom.entity.CourseCompletionHistory
import org.springframework.data.jpa.repository.JpaRepository

interface CourseCompletionHistoryRepository : JpaRepository<CourseCompletionHistory, Long>
