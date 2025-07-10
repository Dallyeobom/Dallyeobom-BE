package kr.dallyeobom.controller.userRanking.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.dto.UserRank

data class UserRankingResponse(
    val list: List<UserRank>,
    @Schema(description = "현재 유저의 랭킹정보 - 기록이 없다면 값이 안내려감")
    val currentUserRank: CurrentUserRank?,
) {
    data class CurrentUserRank(
        @Schema(description = "랭킹")
        val rank: Int,
        @Schema(description = "총 거리")
        val runningLength: Long,
        @Schema(description = "완주한 총 코스 수")
        val completeCourseCount: Long,
    )
}
