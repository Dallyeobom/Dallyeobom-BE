package kr.dallyeobom.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dallyeobom.config.swagger.SwaggerTag
import kr.dallyeobom.controller.user.request.NicknameUpdateRequest
import kr.dallyeobom.controller.user.response.UserInfoResponse
import kr.dallyeobom.util.LoginUserId
import kr.dallyeobom.util.validator.MaxFileSize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.multipart.MultipartFile

@Tag(name = SwaggerTag.USER)
interface UserControllerSpec {
    @Operation(
        summary = "유저 정보를 조회",
        description = "유저 정보를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "닉네임과 프로필 정보",
            ),
        ],
    )
    fun getUserInfo(
        @LoginUserId userId: Long,
    ): UserInfoResponse

    @Operation(
        summary = "닉네임 변경",
        description = "닉네임을 변경합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "닉네임 변경 성공",
            ),
            ApiResponse(
                responseCode = "409",
                description = "중복된 닉네임 존재",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                name = "실패 예시(닉네임 중복)",
                                value = """{"code": 40900,"errorMessage": "이미 존재하는 닉네임입니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun updateNickname(
        @RequestBody @Validated nicknameUpdateRequest: NicknameUpdateRequest,
        @LoginUserId userId: Long,
    )

    @Operation(
        summary = "프로필 사진 변경",
        description = "프로필 사진을 변경합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "프로필 사진 변경 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = """
        잘못된 요청:
        • 파일 사이즈가 1MB를 초과함
      """,
            ),
        ],
    )
    fun updateProfileImage(
        @LoginUserId userId: Long,
        @MaxFileSize
        @Schema(description = "수정하고자 하는 프로필 이미지", required = true)
        profileImage: MultipartFile,
    )
}
