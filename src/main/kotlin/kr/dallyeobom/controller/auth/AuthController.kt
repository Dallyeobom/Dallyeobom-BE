package kr.dallyeobom.controller.auth

import kr.dallyeobom.controller.auth.request.KakaoLoginRequest
import kr.dallyeobom.controller.auth.request.KakaoUserCreateRequest
import kr.dallyeobom.controller.auth.response.KakaoLoginResponse
import kr.dallyeobom.controller.auth.response.NicknameCheckResponse
import kr.dallyeobom.service.UserService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userService: UserService,
) : AuthControllerSpec {
    @GetMapping("/check-nickname")
    override fun checkNickname(
        @RequestParam nickname: String,
    ): NicknameCheckResponse = userService.checkDuplicatedNickName(nickname)

    @PostMapping("/login/kakao")
    override fun kakaoLogin(
        @RequestBody @Validated kakaoLoginRequest: KakaoLoginRequest,
    ): KakaoLoginResponse = userService.kakaoLogin(kakaoLoginRequest)

    @PostMapping("/user/kakao")
    override fun createKakaoUser(
        @RequestBody @Validated kakaoUserCreateRequest: KakaoUserCreateRequest,
    ) = userService.createUser(kakaoUserCreateRequest)

    @Deprecated("로그인 개발을 위한 provider 엑세스토큰 확인 API")
    @GetMapping("/login/kakao")
    fun getToken(
        @RequestParam code: String,
    ) = userService.getProviderAccessToken(code)
}
