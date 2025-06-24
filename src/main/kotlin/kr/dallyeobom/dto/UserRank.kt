package kr.dallyeobom.dto

import java.io.Serializable

data class UserRank(
    val userId: Long,
    val nickname: String,
    val runningLength: Long,
    val completeCourseCount: Long,
) : Serializable
