package kr.dallyeobom.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table
class UserRunningCourse(
    // 유저가 코스를 선택하고 달리는 경우에만 해당 엔티티가 생성된다. 코스 없이 달리는 경우 해당 X
    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    val course: Course,
    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    val user: User,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    val id: Long = 0L,
) : BaseModifiableEntity() {
    // 코스를 변경하지 않아도 갱신 시간을 반영하기 위해 사용
    fun refreshModifiedDateTime() {
        this.updatedDateTime = LocalDateTime.now()
    }
}
