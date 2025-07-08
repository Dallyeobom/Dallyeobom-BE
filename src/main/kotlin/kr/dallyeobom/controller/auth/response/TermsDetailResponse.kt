package kr.dallyeobom.controller.auth.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.entity.TermsTypes
import java.time.LocalDate

data class TermsDetailResponse(
    @Schema(description = "약관 ID", example = "1")
    val id: Long,
    @Schema(description = "약관 타입", example = "SERVICE")
    val type: TermsTypes,
    @Schema(description = "약관 제목", example = "달려봄 서비스 이용약관")
    val name: String,
    @Schema(description = "약관 내용", example = "약관 내용")
    val conditions: String,
    @Schema(description = "개정 날짜", example = "2025-01-01")
    val revisionDate: LocalDate,
    @Schema(description = "필수 여부", example = "true")
    val required: Boolean,
)
