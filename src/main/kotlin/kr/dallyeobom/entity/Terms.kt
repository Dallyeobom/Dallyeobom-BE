package kr.dallyeobom.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table
class Terms(
    @Lob
    @Column(nullable = false, updatable = false, length = 50)
    val conditions: String,
    @Column(nullable = false, updatable = false, length = 50)
    val name: String,
    @Column(nullable = false, updatable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val type: TermsTypes,
    @Column(nullable = false, columnDefinition = "NUMBER(1)")
    val required: Boolean,
    @Column(nullable = false, updatable = false)
    val revisionDate: LocalDate,
    @Column(nullable = false, updatable = true, columnDefinition = "NUMBER(1)")
    val deleted: Boolean,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    val id: Long = 0L,
) : BaseTimeEntity()

enum class TermsTypes(
    val seq: Int,
) {
    SERVICE(1),
    PRIVACY(2),
    PUSH(3),
}
