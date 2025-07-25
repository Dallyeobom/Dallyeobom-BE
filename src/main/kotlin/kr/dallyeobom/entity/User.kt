package kr.dallyeobom.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate

@Entity
@Table(
    name = "users", // Oracle에선 user라는 이름이 예약어라 users로 변경
)
@DynamicUpdate
class User(
    @Column(length = 20, nullable = false, updatable = true, unique = true)
    var nickname: String,
    @Column(length = 30, nullable = false, updatable = false, unique = true)
    val email: String,
    @Column(updatable = true, length = 60)
    val profileImage: String?,
    @Column(length = 200)
    var fcmToken: String? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    val id: Long = 0L,
) : BaseModifiableEntity() {
    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    companion object {
        fun createUser(
            nickname: String,
            email: String,
        ): User = User(nickname, email, null)
    }
}
