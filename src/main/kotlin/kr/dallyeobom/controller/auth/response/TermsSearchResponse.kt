package kr.dallyeobom.controller.auth.response

import kr.dallyeobom.entity.TermsTypes

data class TermsSearchResponse(
    val id: Long,
    val seq: Int,
    val type: TermsTypes,
    val name: String,
    val required: Boolean,
)
