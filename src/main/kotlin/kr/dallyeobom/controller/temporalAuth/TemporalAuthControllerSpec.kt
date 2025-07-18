package kr.dallyeobom.controller.temporalAuth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dallyeobom.config.swagger.SwaggerTag
import kr.dallyeobom.controller.auth.response.ServiceTokensResponse
import kr.dallyeobom.controller.temporalAuth.request.CreateUserRequest
import kr.dallyeobom.controller.temporalAuth.response.TemporalUserResponse

@Tag(name = SwaggerTag.TEMPORAL_AUTH)
interface TemporalAuthControllerSpec {
    @Operation(
        summary = "유저 리스트 조회",
        description = "유저 리스트를 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "유저 리스트 조회 성공"),
        ],
    )
    fun getUsers(): List<TemporalUserResponse>

    @Operation(
        summary = "유저 생성",
        description = "유저를 생성합니다.",
        responses = [
            ApiResponse(responseCode = "201", description = "유저 생성 성공"),
        ],
    )
    fun createUser(request: CreateUserRequest)

    @Operation(
        summary = "유저 로그인",
        description = "유저를 로그인합니다.",
        parameters = [
            Parameter(
                name = "userId",
                description = "로그인할 유저 아이디",
                required = true,
            ),
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
        ],
    )
    fun temporalLogin(userId: Long): ServiceTokensResponse
}
