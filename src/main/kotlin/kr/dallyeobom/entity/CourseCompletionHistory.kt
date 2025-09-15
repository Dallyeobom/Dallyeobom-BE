package kr.dallyeobom.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.locationtech.jts.geom.LineString
import java.time.Duration
import kotlin.math.abs

@Entity
@Table
class CourseCompletionHistory(
    // 유저가 코스를 선택하고 달린 경우에만 코스를 연결
    @ManyToOne
    @JoinColumn
    var course: Course?,
    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    val user: User,
    @Column(length = 300)
    var review: String?,
    @Convert(converter = DurationIntervalConverter::class)
    @Column(columnDefinition = "INTERVAL DAY", nullable = false, updatable = false)
    val interval: Duration,
    @Column(columnDefinition = "SDO_GEOMETRY", nullable = false, updatable = false)
    val path: LineString,
    @Column(nullable = false)
    val length: Int,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    val id: Long = 0L,
) : BaseTimeEntity()

@Converter(autoApply = false)
class DurationIntervalConverter : AttributeConverter<Duration, String> {
    override fun convertToDatabaseColumn(duration: Duration): String {
        val seconds = duration.seconds
        val absSeconds = abs(seconds)
        val days = absSeconds / 86400
        val hours = (absSeconds % 86400) / 3600
        val minutes = (absSeconds % 3600) / 60
        val secs = absSeconds % 60

        return String.format("+%02d %02d:%02d:%02d.00", days, hours, minutes, secs)
    }

    override fun convertToEntityAttribute(dbData: String): Duration {
        val parts = dbData.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val timeParts = parts[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val days = parts[0].toLong()
        val hours = timeParts[0].toLong()
        val minutes = timeParts[1].toLong()
        val seconds =
            timeParts[2]
                .split("\\.".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()[0]
                .toLong()

        return Duration
            .ofDays(days)
            .plusHours(hours)
            .plusMinutes(minutes)
            .plusSeconds(seconds)
    }
}
