package kr.dallyeobom.controller.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import kr.dallyeobom.config.swagger.SwaggerTag
import kr.dallyeobom.controller.auth.request.KakaoLoginRequest
import kr.dallyeobom.controller.auth.request.KakaoUserCreateRequest
import kr.dallyeobom.controller.auth.response.KakaoLoginResponse
import kr.dallyeobom.controller.auth.response.NicknameCheckResponse
import kr.dallyeobom.controller.temporalAuth.response.ServiceTokensResponse
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = SwaggerTag.AUTH)
interface AuthControllerSpec {
    @Operation(
        summary = "닉네임 중복 체크 API",
        description = "닉네임 중복 체크를 진행합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "중복 체크 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = NicknameCheckResponse::class),
                        examples = [
                            ExampleObject(
                                name = "사용 가능 닉네임",
                                description = "중복 되지 않은 닉네임일 경우",
                                value = """{"isDuplicated": false}""",
                            ),
                            ExampleObject(
                                name = "중복된 닉네임",
                                description = "중복된 닉네임일 경우",
                                value = """{"isDuplicated": true}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun checkNickname(
        @RequestParam("nickname") @Valid @Size(min = 2, max = 15) nickname: String,
    ): NicknameCheckResponse

    @Operation(
        summary = "카카오 로그인 API",
        description = "카카오 로그인을 합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "카카오 로그인 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = KakaoLoginResponse::class),
                        examples = [
                            ExampleObject(
                                name = "기존 회원",
                                description = "기존 유저 - 토큰 반환",
                                value = """{"accessToken": "ACCESS_TOKEN_EXAMPLE",
                                    "refreshToken": "REFRESH_TOKEN_EXAMPLE","isNewUser": false}""",
                            ),
                            ExampleObject(
                                name = "신규 회원",
                                description = "신규 유저 - 이메일만 반환",
                                value = """{"accessToken": null,"refreshToken": null,"isNewUser": true}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun kakaoLogin(
        @RequestBody @Valid kakaoLoginRequest: KakaoLoginRequest,
    ): KakaoLoginResponse

    @Operation(
        summary = "카카오 신규 유저 회원가입",
        description = "카카오 로그인 후 신규 유저가 닉네임을 입력해 회원가입을 진행합니다. providerAccessToken은 카카오 로그인 성공 후 받은 provider accessToken을 사용합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "회원가입 성공 - 서비스 토큰 반환",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ServiceTokensResponse::class),
                        examples = [
                            ExampleObject(
                                name = "성공 예시",
                                value = """{"accessToken": "ACCESS_TOKEN_EXAMPLE", "refreshToken": "REFRESH_TOKEN_EXAMPLE"}""",
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 존재하는 닉네임 또는 카카오 계정",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ServiceTokensResponse::class),
                        examples = [
                            ExampleObject(
                                name = "실패 예시",
                                value = """{"code": 40901, "errorMessage": "이미 존재하는 유저입니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun createKakaoUser(
        @RequestBody @Valid kakaoUserCreateRequest: KakaoUserCreateRequest,
    ): ServiceTokensResponse
}
