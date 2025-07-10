package kr.dallyeobom.exception

class TermsDetailNotFoundException :
    BaseException(
        ErrorCode.TERMS_NOT_FOUND,
        "해당 약관을 찾을 수 없습니다.",
    )
