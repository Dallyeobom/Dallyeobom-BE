package kr.dallyeobom.repository

import kr.dallyeobom.entity.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : JpaRepository<Course, Long> {
    @Query(
        """
        SELECT c.*
        FROM COURSE c
        WHERE SDO_NN(
            c.start_point,
            SDO_GEOMETRY(
                  2001, 4326,
                  SDO_POINT_TYPE(:latitude, :longitude,  NULL),
                  NULL, NULL
            ),
            'sdo_num_res=' || :maxCount || ' distance=' || :radius || ' unit=meter',
            1
        ) = 'TRUE' and DELETED_DATETIME is NULL
        ORDER BY SDO_NN_DISTANCE(1)
        """,
        nativeQuery = true,
    )
    fun findNearByCourseByLocation(
        longitude: Double,
        latitude: Double,
        radius: Int,
        maxCount: Int,
    ): List<Course>
}
