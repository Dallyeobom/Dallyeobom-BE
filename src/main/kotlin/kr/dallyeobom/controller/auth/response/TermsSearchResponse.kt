package kr.dallyeobom.controller.auth.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dallyeobom.entity.TermsTypes

data class TermsSearchResponse(
    @Schema(description = "약관 ID", example = "1")
    val id: Long,
    @Schema(description = "약관 뷰 순서", example = "1")
    val seq: Int,
    @Schema(description = "약관 타입", example = "SERVICE")
    val type: TermsTypes,
    @Schema(description = "약관명", example = "달려봄 서비스 이용약관")
    val name: String,
    @Schema(description = "약관 필수여부", example = "true")
    val required: Boolean,
)
