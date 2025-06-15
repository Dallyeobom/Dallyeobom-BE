package kr.dallyeobom.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.LineString
import java.time.Duration

@Entity
@Table
class CourseCompletionHistory(
    // 유저가 코스를 선택하고 달린 경우에만 코스를 연결
    @ManyToOne
    @JoinColumn
    val course: Course?,
    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    val user: User,
    @Column(nullable = false, length = 300)
    var review: String,
    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    @Column(columnDefinition = "INTERVAL DAY", nullable = false, updatable = false)
    val interval: Duration,
    @Column(columnDefinition = "SDO_GEOMETRY", nullable = false, updatable = false)
    val path: LineString,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    val id: Long = 0L,
) : BaseTimeEntity()
