package kr.dallyeobom.controller.temporalAuth

import kr.dallyeobom.controller.temporalAuth.request.CreateUserRequest
import kr.dallyeobom.controller.temporalAuth.response.TemporalUserResponse
import kr.dallyeobom.service.UserService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/temporal")
class TemporalAuthController(
    private val userService: UserService,
) : TemporalAuthControllerSpec {
    @GetMapping("/users")
    override fun getUsers(): List<TemporalUserResponse> = userService.getUsers()

    @ResponseStatus(CREATED)
    @PostMapping("/create")
    override fun createUser(
        @RequestBody request: CreateUserRequest,
    ) {
        userService.createUser(request)
    }

    @PostMapping("/login")
    override fun temporalLogin(
        @RequestParam("userId") userId: Long,
    ) = userService.temporalLogin(userId)
}
