package kr.dallyeobom.exception

class RecentTermsPolicyException :
    BaseException(
        ErrorCode.INVALID_RECENT_TERMS_POLICY,
        "현재 이용약관이어야 합니다.",
    )
