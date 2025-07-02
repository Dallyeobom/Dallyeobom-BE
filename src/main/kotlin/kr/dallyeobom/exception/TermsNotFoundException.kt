package kr.dallyeobom.exception

import kr.dallyeobom.entity.TermsTypes

class TermsNotFoundException(
    termsTypes: TermsTypes,
) : BaseException(
        ErrorCode.TERMS_NOT_FOUND,
        "해당 약관을 찾을 수 없습니다. type = $termsTypes",
    )
