package kr.dallyeobom.repository

import kr.dallyeobom.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByNickname(nickName: String): Boolean

    fun existsByNicknameAndIdNot(
        nickname: String,
        userId: Long,
    ): Boolean
}
