package kr.dallyeobom.exception

class CourseCompletionImageNotFoundException(
    id: Long,
) : BaseException(
        ErrorCode.COURSE_COMPLETION_IMAGE_NOT_FOUND,
        "${id}에 해당하는 코스 완주 인증샷을 찾을 수 없습니다.",
    )
