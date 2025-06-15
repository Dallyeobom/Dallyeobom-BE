package kr.dallyeobom.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table
class BadgeReceiptHistory(
    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    val badgeType: BadgeType,
    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    val user: User,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    val id: Long = 0L,
) : BaseTimeEntity()
