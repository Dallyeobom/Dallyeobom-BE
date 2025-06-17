package kr.dallyeobom.client

import com.fasterxml.jackson.annotation.JsonProperty
import kr.dallyeobom.util.toFormUrlEncoded
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class KakaoApiClient(
    private val clientRegistrationRepository: ClientRegistrationRepository
) {
    private val kakaoRegistration: ClientRegistration
        get() = clientRegistrationRepository.findByRegistrationId("kakao")
            ?: error("Kakao provider가 존재하지 않습니다.")

    val kakaoTokenRestClient = RestClient.create(kakaoRegistration.providerDetails.tokenUri)
    val kakaoUserInfoRestClient = RestClient.create(kakaoRegistration.providerDetails.userInfoEndpoint.uri)

    fun getToken(code: String) = kakaoTokenRestClient.post()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(
            mapOf(
                "grant_type" to "authorization_code",
                "client_id" to kakaoRegistration.clientId,
                "redirect_uri" to kakaoRegistration.redirectUri,
                "code" to code,
                "client_secret" to kakaoRegistration.clientSecret
            ).toFormUrlEncoded()
        )
        .retrieve()
        .body(KakaoTokenResponse::class.java)

    fun getKakaoProfile(accessToken: String) = kakaoUserInfoRestClient.get()
        .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        .retrieve()
        .body(KakaoProfileResponse::class.java)
}

data class KakaoTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("token_type")
    val tokenType: String,
    @JsonProperty("refresh_token")
    val refreshToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    val scope: String,
    @JsonProperty("refresh_token_expires_in")
    val refreshTokenExpiresIn: Int
)

data class KakaoProfileResponse(
    val id: Long,

    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount
)

data class KakaoAccount(
    val email: String?,

    @JsonProperty("profile")
    val profile: KakaoProfile?
)

data class KakaoProfile(
    val nickname: String?,

    @JsonProperty("profile_image_url")
    val profileImageUrl: String?,

    @JsonProperty("thumbnail_image_url")
    val thumbnailImageUrl: String?
)