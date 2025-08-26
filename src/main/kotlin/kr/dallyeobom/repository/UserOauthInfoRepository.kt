package kr.dallyeobom.repository

import kr.dallyeobom.entity.Provder
import kr.dallyeobom.entity.User
import kr.dallyeobom.entity.UserOauthInfo
import org.springframework.data.jpa.repository.JpaRepository

interface UserOauthInfoRepository : JpaRepository<UserOauthInfo, Long> {
    fun existsByProviderUserIdAndProvider(
        providerUserId: String,
        provider: Provder,
    ): Boolean

    fun findByProviderUserIdAndProvider(
        providerUserId: String,
        provider: Provder,
    ): UserOauthInfo?

    fun deleteByUser(user: User)
}
