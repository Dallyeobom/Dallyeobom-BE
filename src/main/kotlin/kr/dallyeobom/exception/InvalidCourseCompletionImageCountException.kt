package kr.dallyeobom.exception

class InvalidCourseCompletionImageCountException :
    BaseException(
        ErrorCode.INVALID_COURSE_COMPLETION_IMAGE_COUNT,
        ErrorCode.INVALID_COURSE_COMPLETION_IMAGE_COUNT.errorMessage,
    )
