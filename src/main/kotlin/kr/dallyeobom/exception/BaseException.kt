package kr.dallyeobom.exception

open class BaseException(
    val errorCode: ErrorCode,
    message: String?,
) : RuntimeException(message)
