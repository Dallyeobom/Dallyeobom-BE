package kr.dallyeobom.util

import com.google.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object CourseLengthUtil {
    private const val EARTH_RADIUS = 6371000.0 // 지구 반지름 (미터 단위)

    fun calculateTotalDistance(points: List<LatLng>): Int {
        if (points.size < 2) return 0

        return points
            .zipWithNext()
            .sumOf { (p1, p2) -> haversine(p1, p2) }
            .toInt() // 단위: 미터 (m)
    }

    private fun haversine(
        p1: LatLng,
        p2: LatLng,
    ): Double {
        val dLat = Math.toRadians(p2.lat - p1.lat)
        val dLng = Math.toRadians(p2.lng - p1.lng)
        val a =
            sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(p1.lat)) *
                cos(Math.toRadians(p2.lat)) *
                sin(dLng / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS * c
    }
}
