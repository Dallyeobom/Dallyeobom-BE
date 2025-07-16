package kr.dallyeobom.exception

class InvalidCourseCompletionImageCountException(
    message: String,
) : BaseException(
        ErrorCode.INVALID_COURSE_COMPLETION_IMAGE_COUNT,
        message,
    )
