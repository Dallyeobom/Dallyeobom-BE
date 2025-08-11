package kr.dallyeobom.controller.user

import kr.dallyeobom.controller.user.request.NicknameUpdateRequest
import kr.dallyeobom.controller.user.response.UserInfoResponse
import kr.dallyeobom.service.UserService
import kr.dallyeobom.util.LoginUserId
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

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

    @PutMapping("/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    override fun updateProfileImage(
        @LoginUserId userId: Long,
        @RequestPart
        profileImage: MultipartFile,
    ) = userService.updateProfileImage(userId, profileImage)

    @DeleteMapping("/profile-image")
    override fun deleteProfileImage(userId: Long) = userService.deleteProfileImage(userId)


}
