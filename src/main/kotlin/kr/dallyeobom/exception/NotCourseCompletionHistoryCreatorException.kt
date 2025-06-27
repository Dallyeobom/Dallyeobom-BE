package kr.dallyeobom.exception

class NotCourseCompletionHistoryCreatorException :
    BaseException(
        ErrorCode.NOT_COURSE_COMPLETION_HISTORY_CREATOR,
        ErrorCode.NOT_COURSE_COMPLETION_HISTORY_CREATOR.errorMessage,
    )
