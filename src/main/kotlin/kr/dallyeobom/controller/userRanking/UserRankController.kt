package kr.dallyeobom.controller.userRanking

import kr.dallyeobom.controller.userRanking.response.UserRankingResponse
import kr.dallyeobom.service.UserRankService
import kr.dallyeobom.util.LoginUserId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/user-rank")
@RestController
class UserRankController(
    private val userRankService: UserRankService,
) : UserRankControllerSpec {
    @GetMapping("/{type}")
    override fun getUserRanking(
        @LoginUserId
        userId: Long,
        @PathVariable type: UserRankType,
    ): UserRankingResponse = userRankService.getUserRanking(userId, type)
}
