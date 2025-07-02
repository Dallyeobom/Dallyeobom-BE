package kr.dallyeobom.controller.common.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive

data class SliceRequest(
    @field:Positive(message = "마지막 조회 ID는 양수여야 합니다.")
    @field:Schema(
        description = "받은 데이터의 마지막 ID - 값이 없으면 처음부터",
        example = "10",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val lastId: Long?,
    @field:Positive(message = "조회하고자 리스트의 크기는 양수여야 합니다.")
    @field:Schema(
        description = "조회하고자 하는 리스트 크기 - 값이 없으면 10",
        example = "10",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val size: Int = 10,
)
