package kr.dallyeobom.exception

class TermsAgreedPolicyException :
    BaseException(
        ErrorCode.ALREADY_EXIST_NICKNAME,
        "필수 이용약관은 동의해야합니다.",
    )
