package kr.dallyeobom.controller.auth.request

import kr.dallyeobom.entity.TermsTypes

data class TermsAgreeRequest(
    val id: Long,
    val termsType: TermsTypes,
    val agreed: Boolean,
)
