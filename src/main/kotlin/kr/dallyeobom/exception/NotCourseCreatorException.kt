package kr.dallyeobom.exception

class NotCourseCreatorException :
    BaseException(
        ErrorCode.NOT_COURSE_CREATOR,
        ErrorCode.NOT_COURSE_CREATOR.errorMessage,
    )
