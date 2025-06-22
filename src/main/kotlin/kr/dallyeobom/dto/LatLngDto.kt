package kr.dallyeobom.dto

import com.google.maps.model.LatLng
import io.swagger.v3.oas.annotations.media.Schema

data class LatLngDto(
    @Schema(description = "위도", example = "37.5665")
    val latitude: Double,
    @Schema(description = "경도", example = "126.978")
    val longitude: Double,
) {
    fun toLatLng(): LatLng = LatLng(latitude, longitude)
}
