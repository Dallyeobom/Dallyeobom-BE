package kr.dallyeobom.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kr.dallyeobom.util.toFormUrlEncoded
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class KakaoApiClient(
    private val clientRegistrationRepository: ClientRegistrationRepository,
) {
    private val kakaoRegistration: ClientRegistration
        get() = clientRegistrationRepository.findByRegistrationId("kakao") ?: error("Kakao provider가 존재하지 않습니다.")

    private val objectMapper: ObjectMapper =
        ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerKotlinModule()

    private val kakaoTokenRestClient = customRestClient(kakaoRegistration.providerDetails.tokenUri)
    private val kakaoUserInfoRestClient = customRestClient(kakaoRegistration.providerDetails.userInfoEndpoint.uri)

    private fun customRestClient(baseUrl: String) =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .messageConverters { converters ->
                converters.removeIf { it is MappingJackson2HttpMessageConverter }
                converters.add(MappingJackson2HttpMessageConverter(objectMapper))
            }.build()

    fun getToken(code: String): KakaoTokenResponse? =
        kakaoTokenRestClient
            .post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                mapOf(
                    "grant_type" to "authorization_code",
                    "client_id" to kakaoRegistration.clientId,
                    "redirect_uri" to kakaoRegistration.redirectUri,
                    "code" to code,
                    "client_secret" to kakaoRegistration.clientSecret,
                ).toFormUrlEncoded(),
            ).retrieve()
            .body(KakaoTokenResponse::class.java)

    fun getKakaoProfile(accessToken: String) =
        kakaoUserInfoRestClient
            .get()
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .retrieve()
            .body(KakaoProfileResponse::class.java)
}

data class KakaoTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val refreshToken: String,
    val expiresIn: Int,
    val scope: String,
    val refreshTokenExpiresIn: Int,
)

data class KakaoProfileResponse(
    val id: String,
    val kakaoAccount: KakaoAccount,
)

data class KakaoAccount(
    val email: String,
    val profile: KakaoProfile,
)

data class KakaoProfile(
    val nickname: String,
    val profileImageUrl: String,
    val thumbnailImageUrl: String,
)
