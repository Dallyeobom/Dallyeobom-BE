package kr.dallyeobom.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table
class TermsAgreeHistory(
    @Column(nullable = false, updatable = false)
    val userId: Long,
    @Column(nullable = false, updatable = false)
    val termsId: Long,
    @Column(nullable = false, columnDefinition = "NUMBER(1)")
    val agreed: Boolean,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    val id: Long = 0L,
) : BaseTimeEntity()
