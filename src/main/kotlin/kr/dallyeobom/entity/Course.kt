package kr.dallyeobom.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point

@Entity
@Table
@SQLDelete(sql = "UPDATE course SET deleted_datetime = current_timestamp WHERE id = ?")
class Course(
    @Column(nullable = false, length = 30)
    var name: String,
    @Column(nullable = false, length = 500)
    var description: String,
    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    var courseLevel: CourseLevel,
    @Column(length = 60)
    var image: String?,
    @Column(nullable = false, length = 50)
    var location: String,
    @Column(nullable = false, length = 60)
    var overviewImage: String,
    @Column(nullable = false, updatable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val creatorType: CourseCreatorType,
    @Column(updatable = false)
    val creatorId: Long?,
    @Column(nullable = false)
    val length: Int,
    @Column(columnDefinition = "SDO_GEOMETRY", nullable = false, updatable = false)
    val startPoint: Point,
    @Column(columnDefinition = "SDO_GEOMETRY", nullable = false, updatable = false)
    val path: LineString,
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    var visibility: CourseVisibility,
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

enum class CourseVisibility {
    PUBLIC,
    PRIVATE,
    // CREW, // 나중에 크루 기능같은게 추가되면 크루에게만 공개 같은 기능을 추가할 수 있음
}
