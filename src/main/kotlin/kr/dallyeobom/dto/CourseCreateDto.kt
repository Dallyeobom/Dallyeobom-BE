package kr.dallyeobom.dto

import com.google.maps.model.LatLng
import kr.dallyeobom.entity.CourseCreatorType
import kr.dallyeobom.entity.CourseLevel
import kr.dallyeobom.util.subStringOrReturn

// 아래 DTO에만 데이터를 담아주면 나머지 데이터는 CourseService::saveCourse 메소드 내부에서 알아서 필요한 데이터 추출해서 사용함
data class CourseCreateDto(
    // Backing Properties
    private val _name: String,
    private val _description: String,
    val courseLevel: CourseLevel,
    val image: String?,
    val creatorType: CourseCreatorType,
    val creatorId: Long?,
    val path: List<LatLng>,
) {
    // 길이 넘는건 자름
    val name = _name.subStringOrReturn(30)
    val description = _description.subStringOrReturn(500) // 두루누비 API에선 1000자 넘는 데이터도 있는데 굳이 다 저장해야할까 싶어서 최대 500자로 자름
}
