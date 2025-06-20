package kr.dallyeobom.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table
class UserOauthInfo(
    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    val user: User,
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val provider: Provder,
    @Column(nullable = false, length = 100)
    val providerUserId: String,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    val id: Long = 0L,
) : BaseTimeEntity() {
    companion object {
        fun createKakaoOauthInfo(
            user: User,
            providerUserId: String,
        ): UserOauthInfo =
            UserOauthInfo(
                user = user,
                Provder.KAKAO,
                providerUserId,
            )
    }
}

enum class Provder {
    KAKAO,
}
