package kr.dallyeobom.client

import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.StaticMapsRequest
import com.google.maps.model.AddressComponent
import com.google.maps.model.EncodedPolyline
import com.google.maps.model.LatLng
import com.google.maps.model.Size
import kr.dallyeobom.config.properties.GoogleMapsProperties
import org.springframework.stereotype.Component

@Component
class GoogleMapsClient(
    googleMapsProperties: GoogleMapsProperties,
) {
    private val context: GeoApiContext =
        GeoApiContext
            .Builder()
            .apiKey(googleMapsProperties.apiKey)
            .build()

    fun getStaticMap(path: List<LatLng>): ByteArray =
        CustomStaticMapsRequest(context, STATIC_MAP_SIZE)
            .path(
                EncodedPolyline(path),
                color = STATIC_MAP_PATH_COLOR,
                weight = STATIC_MAP_PATH_WEIGHT,
            ).scale(STATIC_MAP_SCALE)
            .await()
            .imageData

    fun getLocation(latLng: LatLng): Array<AddressComponent> =
        GeocodingApi
            .reverseGeocode(context, latLng)
            .language("ko")
            .await()
            .firstOrNull()
            ?.addressComponents ?: emptyArray()

    // 기본적으로 encodedPath와 color, weight를 동시에 사용할 수 없으나 우리는 필요하다
    // 따라서 StaticMapsRequest를 상속받아 새로운 메소드를 만든다
    private class CustomStaticMapsRequest(
        context: GeoApiContext,
        size: Size,
    ) : StaticMapsRequest(context) {
        init {
            this.size(size)
        }

        fun path(
            path: EncodedPolyline,
            color: String,
            weight: Int,
        ): StaticMapsRequest = paramAddToList("path", "weight:$weight|color:$color|enc:" + path.encodedPath)
    }

    companion object {
        private const val STATIC_MAP_PATH_COLOR = "0xE38756FF"
        private const val STATIC_MAP_PATH_WEIGHT = 10
        private const val STATIC_MAP_SCALE = 2

        @JvmStatic
        private val STATIC_MAP_SIZE = Size(400, 400)
    }
}
