package kr.dallyeobom.exception

class CourseCompletionHistoryNotFoundException :
    BaseException(
        ErrorCode.COURSE_COMPLETION_HISTORY_NOT_FOUND,
        ErrorCode.COURSE_COMPLETION_HISTORY_NOT_FOUND.errorMessage,
    )
