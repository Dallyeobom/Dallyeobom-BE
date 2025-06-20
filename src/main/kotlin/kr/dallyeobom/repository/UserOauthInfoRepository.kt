package kr.dallyeobom.repository

import kr.dallyeobom.entity.UserOauthInfo
import org.springframework.data.jpa.repository.JpaRepository

interface UserOauthInfoRepository : JpaRepository<UserOauthInfo, Long> {
    fun findByProviderUserId(providerUserId: String): UserOauthInfo?
}
