package kr.dallyeobom.client

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.google.maps.model.LatLng
import kr.dallyeobom.config.properties.TourApiProperties
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class TourApiClient(
    tourApiProperties: TourApiProperties,
) {
    private val restClient: RestClient =
        RestClient
            .builder()
            .baseUrl("https://apis.data.go.kr")
            .build()

    val defaultQueryParams =
        MultiValueMap.fromSingleValue(
            mapOf(
                "serviceKey" to tourApiProperties.apiKey,
                "MobileOS" to "AND",
                "MobileApp" to URLEncoder.encode("달려봄", StandardCharsets.UTF_8),
                "_type" to "json",
            ),
        )

    fun getCourseList(
        numOfRows: Int = 1000,
        pageNo: Int = 1,
    ) = restClient
        .get()
        .uri(
            UriComponentsBuilder
                .fromPath("/B551011/Durunubi/courseList")
                .queryParams(defaultQueryParams)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .build(true)
                .toUri(),
        ).retrieve()
        .body(CourseListResponse::class.java)!!
        .response.body.items.item

    // 1km 반경 내에 있는 관광지 이미지 가져오기 - 그걸로 코스 대표 이미지로 사용
    fun getLocationImageUrl(latLng: LatLng) =
        restClient
            .get()
            .uri(
                UriComponentsBuilder
                    .fromPath("/B551011/KorService2/locationBasedList2")
                    .queryParams(defaultQueryParams)
                    .queryParam("numOfRows", 10)
                    .queryParam("pageNo", 1)
                    .queryParam("arrange", "S") // 정렬 기준: 이미지 있는 데이터중에 거리순
                    .queryParam("mapX", latLng.lng)
                    .queryParam("mapY", latLng.lat)
                    .queryParam("radius", 1000) // 1km 반경
                    .build(true)
                    .toUri(),
            ).retrieve()
            .body(LocationImageResponse::class.java)!!
            .response.body.items
            ?.item
            ?.firstOrNull()
            ?.firstimage

    fun getFileWithStream(fileUrl: String) =
        restClient
            .get()
            .uri(fileUrl)
            .retrieve()
            .body(ByteArray::class.java)
            ?.inputStream()
}

// 왜 이런 복잡한 구조로 되어있는지 모르겠지만, 주는 그대로 받으려면 이렇게 해야한다
data class CourseListResponse(
    val response: Response,
) {
    data class Response(
        val body: ResponseBody,
    )

    data class ResponseBody(
        val items: ResponseBodyItems,
    )

    data class ResponseBodyItems(
        val item: List<ListResponseBodyItem>,
    )

    data class ListResponseBodyItem(
        val crsKorNm: String,
        val crsLevel: String,
        val crsContents: String,
        val gpxpath: String,
    )
}

data class LocationImageResponse(
    val response: Response,
) {
    data class Response(
        val body: ResponseBody,
    )

    data class ResponseBody(
        // 왜 이렇게 만들었는지 모르겠는데 items는 분명 JsonObject인데 값이 없으면 {}나 null로 오지 않고 ""로 온다
        // 그래서 커스텀 디시리얼라이저를 만들어서 ""일 때 null로 처리한다
        @JsonDeserialize(using = ItemsDeserializer::class)
        val items: ResponseBodyItems?,
    )

    data class ResponseBodyItems(
        val item: List<ListResponseBodyItem>,
    )

    data class ListResponseBodyItem(
        val firstimage: String,
    )
}

class ItemsDeserializer : JsonDeserializer<LocationImageResponse.ResponseBodyItems>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): LocationImageResponse.ResponseBodyItems? {
        val mapper = (p.codec as ObjectMapper)
        val node = p.codec.readTree<JsonNode>(p)

        return when {
            node.isTextual && node.asText() == "" -> {
                null
            }
            else -> {
                mapper.readValue(
                    node.traverse(mapper),
                    object : TypeReference<LocationImageResponse.ResponseBodyItems>() {},
                )
            }
        }
    }
}
