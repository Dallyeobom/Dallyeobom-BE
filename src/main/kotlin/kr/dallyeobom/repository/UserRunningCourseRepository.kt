package kr.dallyeobom.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import kr.dallyeobom.entity.Course
import kr.dallyeobom.entity.User
import kr.dallyeobom.entity.UserRunningCourse
import kr.dallyeobom.util.getAll
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface UserRunningCourseRepository :
    JpaRepository<UserRunningCourse, Long>,
    CustomUserRunningCourseRepository {
    fun findByUser(user: User): List<UserRunningCourse>

    fun deleteByUserId(userId: Long): Int
}

interface CustomUserRunningCourseRepository {
    fun getNearByUserRunningCourse(
        user: User,
        longitude: Double,
        latitude: Double,
        radius: Int,
        maxCount: Int,
    ): List<UserRunningCourse>
}

class CustomUserRunningCourseRepositoryImpl(
    private val kotlinJdslJpqlExecutor: KotlinJdslJpqlExecutor,
) : CustomUserRunningCourseRepository {
    override fun getNearByUserRunningCourse(
        user: User,
        longitude: Double,
        latitude: Double,
        radius: Int,
        maxCount: Int,
    ) = kotlinJdslJpqlExecutor.getAll(maxCount) {
        val entity = entity(UserRunningCourse::class)
        selectDistinct(entity)
            .from(
                entity,
                innerFetchJoin(UserRunningCourse::course),
                innerFetchJoin(UserRunningCourse::user),
            ).whereAnd(
                customExpression(
                    String::class,
                    "SDO_NN({0}, SDO_GEOMETRY(2001, 4326, SDO_POINT_TYPE({1}, {2}, NULL), NULL, NULL), 'distance=' || {3} || ' unit=meter', 1)",
                    path(Course::startPoint),
                    latitude,
                    longitude,
                    radius,
                ).eq("TRUE"),
                path(Course::deletedDateTime).isNull(),
                // 최근 30분 이내에 업데이트된 코스만 조회
                path(UserRunningCourse::updatedDateTime).ge(LocalDateTime.now().minusMinutes(30)),
                path(UserRunningCourse::user).ne(user), // 현재 유저는 제외
            )
    }
}
