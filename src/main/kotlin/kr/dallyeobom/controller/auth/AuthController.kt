package kr.dallyeobom.controller.auth

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
) : AuthControllerSpec {
    @GetMapping("/login/kakao")
    override fun kakaoLogin(
        @RequestParam("code") code: String,
    ): KakaoLoginResponse = userService.kakaoLogin(code)
}
