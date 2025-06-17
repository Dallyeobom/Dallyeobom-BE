package kr.dallyeobom.controller.auth

import kr.dallyeobom.controller.auth.response.KakaoLoginResponse
import kr.dallyeobom.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val userService: UserService,
) {
    @GetMapping("/api/v1/auth/login/kakao")
    fun kakaoLogin(
        @RequestParam("code") code: String,
    ): KakaoLoginResponse = userService.kakaoLogin(code)


}