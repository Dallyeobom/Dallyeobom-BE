package kr.dallyeobom.service

import com.google.maps.model.AddressComponentType
import com.google.maps.model.LatLng
import io.jenetics.jpx.GPX
import kr.dallyeobom.client.GoogleMapsClient
import kr.dallyeobom.client.TourApiClient
import kr.dallyeobom.controller.course.request.NearByCourseSearchRequest
import kr.dallyeobom.controller.course.response.CourseDetailResponse
import kr.dallyeobom.controller.course.response.NearByCourseSearchResponse
import kr.dallyeobom.dto.CourseCreateDto
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.CourseCreatorType
import kr.dallyeobom.entity.CourseLevel
import kr.dallyeobom.exception.BaseException
import kr.dallyeobom.exception.CourseNotFoundException
import kr.dallyeobom.exception.ErrorCode
import kr.dallyeobom.repository.CourseRepository
import kr.dallyeobom.repository.ObjectStorageRepository
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.util.Locale
import java.util.PriorityQueue
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val objectStorageRepository: ObjectStorageRepository,
    private val googleMapsClient: GoogleMapsClient,
    private val tourApiClient: TourApiClient,
) {
    // 관광데이터 API를 통해 경로를 가져오고, DB를 채워넣는 메소드
    // 해당 메소드는 멱등성보장이 되지 않는다. 따라서 여러번 호출하면 중복된 코스가 생성된다.
    fun createCoursesWithTourApi(): List<Course> {
        val courses =
            tourApiClient.getCourseList()
                ?: throw BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "코스 리스트를 가져오는 데 실패했습니다. 관광데이터 API가 정상적으로 작동하는지 확인해주세요.")
        return courses.map { course ->
            val gpx = GPX.Reader.of(GPX.Reader.Mode.LENIENT).read(tourApiClient.getFileWithStream(course.gpxpath))
            val path =
                gpx.tracks
                    .flatMap { it.segments }
                    .flatMap { it.points }
                    .map { LatLng(it.latitude.toDegrees(), it.longitude.toDegrees()) }
            val image =
                tourApiClient.getLocationImageUrl(path.first())?.let { imageUrl ->
                    val fileName =
                        UUID.randomUUID().toString() + "." + // 중복나지 않도록 UUID 사용
                            imageUrl.split(".").last().lowercase(Locale.getDefault()) // 확장자 추출후 소문자로 통일
                    try {
                        tourApiClient.getFileWithStream(imageUrl)?.let { stream ->
                            objectStorageRepository.upload(
                                ObjectStorageRepository.COURSE_IMAGE_PATH,
                                fileName,
                                stream,
                            )
                        }
                    } catch (e: Exception) {
                        // 왜 인지 모르겠는데 해당 API가 실행 안되는 좌표들이 간혹 있다. 500에러 발생하면 재시도 해도 안됨
                        // 이 경우에는 null로 처리
                        null
                    }
                }
            saveCourse(
                CourseCreateDto(
                    course.crsKorNm,
                    course.crsContents,
                    CourseLevel.entries[course.crsLevel.toInt() - 1],
                    image,
                    CourseCreatorType.SYSTEM,
                    null,
                    path,
                ),
            )
        }
    }

    private fun saveCourse(courseDto: CourseCreateDto): Course {
        val fileName = UUID.randomUUID().toString() + ".png" // 중복나지 않도록 UUID 사용, 확장자는 png로 고정되어 있음

        val simplified = simplifyPath(courseDto.path)
        val imageBytes = googleMapsClient.getStaticMap(simplified)

        val path =
            GeometryFactory()
                .createLineString(
                    courseDto.path.map { Coordinate(it.lat, it.lng) }.toTypedArray(),
                ).also {
                    it.srid = WGS84_SRID
                }
        val startPoint =
            GeometryFactory()
                .createPoint(
                    Coordinate(
                        courseDto.path.first().lat,
                        courseDto.path.first().lng,
                    ),
                ).also {
                    it.srid = WGS84_SRID
                }

        // 코스의 시작좌표를 기준으로 해당 코스의 지역을 결정함
        val reverseGeocodingResult = googleMapsClient.getLocation(courseDto.path.first())

        val location =
            (
                // 도
                reverseGeocodingResult
                    .firstOrNull { it.types.contains(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1) }
                    ?.shortName
                    ?.let { "$it " } ?: ""
            ) +
                (
                    // 시 혹은 군
                    reverseGeocodingResult
                        .firstOrNull { it.types.contains(AddressComponentType.LOCALITY) }
                        ?.shortName
                        ?.let { "$it " } ?: ""
                ) +
                (
                    // 구, 면, 읍 중 하나
                    reverseGeocodingResult
                        .firstOrNull { it.types.contains(AddressComponentType.SUBLOCALITY_LEVEL_2) }
                        ?.shortName ?: ""
                )

        return courseRepository.save(
            Course(
                name = courseDto.name,
                description = courseDto.description,
                courseLevel = courseDto.courseLevel,
                image = courseDto.image,
                location = location,
                creatorType = courseDto.creatorType,
                creatorId = courseDto.creatorId,
                path = path,
                overviewImage =
                    objectStorageRepository.upload(
                        ObjectStorageRepository.COURSE_OVERVIEW_IMAGE_PATH,
                        fileName,
                        ByteArrayInputStream(imageBytes),
                    ),
                length = calculateTotalDistance(courseDto.path),
                startPoint = startPoint,
            ),
        )
    }

    // 어차피 썸네일에선 경로를 자세하게 보여줄 필요가 없으므로 효율적인 표현을 위해
    // VisvalingamWhyatt 알고리즘을 사용하여 경로를 단순화
    fun simplifyPath(points: List<LatLng>): List<LatLng> {
        if (points.size <= MAX_SIMPLIFIED_POINTS) return points.toList()

        val n = points.size
        val prev = IntArray(n) { it - 1 }.also { it[0] = -1 }
        val next = IntArray(n) { it + 1 }.also { it[n - 1] = -1 }
        val area = DoubleArray(n) { Double.POSITIVE_INFINITY }

        for (i in 1 until n - 1) area[i] = triangleArea(points[prev[i]], points[i], points[next[i]])

        val pq = PriorityQueue<Int> { a, b -> area[a].compareTo(area[b]) }
        for (i in 1 until n - 1) pq.add(i)

        var remaining = n
        while (remaining > MAX_SIMPLIFIED_POINTS && pq.isNotEmpty()) {
            val idx = pq.poll()
            if (area[idx].isNaN()) continue

            val p = prev[idx]
            val q = next[idx]
            if (p != -1) next[p] = q
            if (q != -1) prev[q] = p
            remaining--
            area[idx] = Double.NaN

            if (p != -1 && prev[p] != -1) {
                area[p] = triangleArea(points[prev[p]], points[p], points[q])
                pq.add(p)
            }
            if (q != -1 && next[q] != -1) {
                area[q] = triangleArea(points[p], points[q], points[next[q]])
                pq.add(q)
            }
        }

        val result = ArrayList<LatLng>(remaining)
        var i = 0
        while (i != -1) {
            result.add(points[i])
            i = next[i]
        }
        return result
    }

    private fun triangleArea(
        a: LatLng,
        b: LatLng,
        c: LatLng,
    ): Double {
        val ax = a.lng
        val ay = a.lat
        val bx = b.lng
        val by = b.lat
        val cx = c.lng
        val cy = c.lat
        return abs((ax * (by - cy) + bx * (cy - ay) + cx * (ay - by)) * 0.5)
    }

    private fun calculateTotalDistance(points: List<LatLng>): Int {
        if (points.size < 2) return 0

        var total = 0.0
        for (i in 0 until points.lastIndex) {
            total += haversine(points[i], points[i + 1])
        }
        return total.toInt() // 단위: 미터 (m)
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

    fun searchNearByLocation(request: NearByCourseSearchRequest): List<NearByCourseSearchResponse> =
        courseRepository
            .findNearByCourseByLocation(
                request.longitude,
                request.latitude,
                request.radius,
                request.maxCount,
            ).map { NearByCourseSearchResponse.from(it, objectStorageRepository.getDownloadUrl(it.overviewImage)) }

    fun getCourseDetail(id: Long): CourseDetailResponse {
        val course = courseRepository.findById(id).getOrNull() ?: throw CourseNotFoundException()
        return CourseDetailResponse.from(
            course,
            course.image?.let { objectStorageRepository.getDownloadUrl(it) },
            objectStorageRepository.getDownloadUrl(course.overviewImage),
        )
    }

    companion object {
        private const val WGS84_SRID = 4326 // WGS84 SRID
        private const val MAX_SIMPLIFIED_POINTS = 1000 // 최대 단순화된 포인트 수
        private const val EARTH_RADIUS = 6371000.0 // 지구 반지름 (미터 단위)
    }
}
