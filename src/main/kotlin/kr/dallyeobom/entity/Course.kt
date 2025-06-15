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
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point

@Entity
@Table
@SQLDelete(sql = "UPDATE course SET deleted_datetime = current_timestamp WHERE id = ?")
@SQLRestriction("deleted_datetime IS NULL")
class Course(
    @Column(nullable = false, length = 20)
    var name: String,
    @Column(nullable = false, length = 500)
    var description: String,
    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    var courseLevel: CourseLevel,
    @Column(length = 50)
    var image: String?,
    @Column(nullable = false, length = 50)
    var location: String,
    @Column(nullable = false, length = 50)
    var overviewImage: String,
    @Column(nullable = false, updatable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val creatorType: CourseCreatorType,
    @ManyToOne
    @JoinColumn(updatable = false)
    val creator: User?,
    @Column(nullable = false)
    val length: Int,
    @Column(columnDefinition = "SDO_GEOMETRY", nullable = false, updatable = false)
    val startPoint: Point,
    @Column(columnDefinition = "SDO_GEOMETRY", nullable = false, updatable = false)
    val path: LineString,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    val id: Long = 0L,
) : BaseSoftDeletableEntity()

enum class CourseLevel {
    LOW,
    MIDDLE,
    HIGH,
}

enum class CourseCreatorType {
    USER,
    SYSTEM,
}
