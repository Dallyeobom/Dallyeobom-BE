package kr.dallyeobom.controller.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kr.dallyeobom.controller.auth.response.KakaoLoginResponse
import kr.dallyeobom.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userService: UserService,
) {
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
                                value = """{"accessToken": "ACCESS_TOKEN_EXAMPLE","refreshToken": "REFRESH_TOKEN_EXAMPLE","isNewUser": false,"email": null}""",
                            ), ExampleObject(
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
    @GetMapping("/login/kakao")
    fun kakaoLogin(
        @RequestParam("code") code: String,
    ): KakaoLoginResponse = userService.kakaoLogin(code)
}
