package kr.dallyeobom.exception

class RequiredTermsAgreedPolicyException :
    BaseException(
        ErrorCode.INVALID_REQUIRED_TERMS_POLICY,
        "필수 이용약관은 동의해야합니다.",
    )
