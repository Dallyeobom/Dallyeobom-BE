package kr.dallyeobom.controller.userRanking

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dallyeobom.config.swagger.SwaggerTag
import kr.dallyeobom.controller.userRanking.response.UserRankingResponse

@Tag(name = SwaggerTag.USER_RANK)
interface UserRankControllerSpec {
    @Operation(
        summary = "유저 랭킹 조회",
        description = "유저 랭킹을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "유저 랭킹 정보"),
        ],
    )
    fun getUserRanking(
        @Schema(description = "조회하고자 하는 랭킹 타입", example = "WEEKLY")
        type: UserRankType,
    ): UserRankingResponse
}

enum class UserRankType {
    WEEKLY,
    MONTHLY,
    YEARLY,
}
