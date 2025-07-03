package kr.dallyeobom.exception

import kr.dallyeobom.entity.TermsTypes

class TermsNotFoundException(
    termsTypes: TermsTypes,
) : BaseException(
        ErrorCode.TERMS_NOT_FOUND,
        "해당 약관 항목이 누락되었습니다. type = $termsTypes",
    )
