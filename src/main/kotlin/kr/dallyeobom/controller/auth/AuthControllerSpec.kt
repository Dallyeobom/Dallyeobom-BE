package kr.dallyeobom.controller.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dallyeobom.config.swagger.SwaggerTag
import kr.dallyeobom.controller.auth.response.KakaoLoginResponse
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = SwaggerTag.AUTH)
interface AuthControllerSpec {
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
                                    "refreshToken": "REFRESH_TOKEN_EXAMPLE","isNewUser": false,"email": null}""",
                            ),
                            ExampleObject(
                                name = "신규 회원",
                                description = "신규 유저 - 이메일만 반환",
                                value = """{"accessToken": null,"refreshToken": null,"isNewUser": true,"email": "example@kakao.com"}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun kakaoLogin(
        @RequestParam("code") code: String,
    ): KakaoLoginResponse
}
