package kr.dallyeobom.controller.health

import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

// K8S에서 헬스 체크를 위한 컨트롤러
@Hidden
@RestController
class HealthController {
    @GetMapping("/health")
    fun health(): HealthResponse = HealthResponse(status = HttpServletResponse.SC_OK)

    @GetMapping("/ready")
    fun ready() {}
}

data class HealthResponse(
    val status: Int,
)
