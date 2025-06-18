package kr.dallyeobom.exception

class CourseNotFoundException :
    BaseException(
        ErrorCode.COURSE_NOT_FOUND,
        ErrorCode.COURSE_NOT_FOUND.errorMessage,
    )
