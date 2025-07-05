package kr.dallyeobom.dto

import kr.dallyeobom.entity.User
import java.time.Duration

data class CourseRankingInfo(
    val user: User,
    val interval: Duration,
)
