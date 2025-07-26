package kr.dallyeobom.controller.user

import kr.dallyeobom.controller.auth.request.NicknameUpdateRequest
import kr.dallyeobom.controller.auth.response.UserInfoResponse
import kr.dallyeobom.service.UserService
import kr.dallyeobom.util.LoginUserId
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService,
) : UserControllerSpec {
    @GetMapping
    override fun getUserInfo(
        @LoginUserId userId: Long,
    ): UserInfoResponse = userService.getUserInfo(userId)

    @PutMapping("/nickname")
    override fun updateNickname(
        @RequestBody @Validated nicknameUpdateRequest: NicknameUpdateRequest,
        @LoginUserId userId: Long,
    ) = userService.updateNickname(nicknameUpdateRequest, userId)
}
