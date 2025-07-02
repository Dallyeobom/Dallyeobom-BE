package kr.dallyeobom.exception

class TermsAgreedPolicyException :
    BaseException(
        ErrorCode.INVALID_TERMS_POLICY,
        "필수 이용약관은 동의해야합니다.",
    )
