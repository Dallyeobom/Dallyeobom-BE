package kr.dallyeobom.controller.exceptionHandler

import jakarta.validation.ConstraintViolationException
import kr.dallyeobom.exception.BaseException
import kr.dallyeobom.exception.ErrorCode
import kr.dallyeobom.exception.ErrorResponse
import kr.dallyeobom.util.logging.TraceManager
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException
import org.apache.tomcat.util.http.fileupload.impl.SizeException
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CustomExceptionHandler(
    private val traceManager: TraceManager,
) {
    @ExceptionHandler(
        IllegalArgumentException::class,
        IllegalStateException::class,
        ConstraintViolationException::class,
    )
    fun invalidRequestException(ex: RuntimeException): ResponseEntity<Any> {
        traceManager.doErrorLog(ex)
        return getInvalidRequestResponse(ex.message)
    }

    @ExceptionHandler(SizeLimitExceededException::class, FileSizeLimitExceededException::class)
    fun sizeLimitExceededException(ex: SizeException): ResponseEntity<ErrorResponse> {
        traceManager.doErrorLog(ex)
        val payloadTooLargeErrorCode = ErrorCode.PAYLOAD_TOO_LARGE
        return ResponseEntity
            .status(payloadTooLargeErrorCode.httpStatus)
            .body(ErrorResponse.of(payloadTooLargeErrorCode, ex.message))
    }

    @ExceptionHandler(BaseException::class)
    fun baseException(ex: BaseException): ResponseEntity<ErrorResponse> {
        traceManager.doErrorLog(ex)
        return ResponseEntity
            .status(ex.errorCode.httpStatus)
            .body(ErrorResponse.of(ex.errorCode, ex.message))
    }

    @ExceptionHandler(Exception::class)
    fun exception(ex: Exception): ResponseEntity<ErrorResponse> {
        traceManager.doErrorLog(ex)
        val internalServerErrorCode = ErrorCode.INTERNAL_SERVER_ERROR
        return ResponseEntity
            .status(internalServerErrorCode.httpStatus)
            .body(ErrorResponse.of(internalServerErrorCode, ex.message))
    }

    private fun getInvalidRequestResponse(
        errorMessage: String?,
        httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
    ): ResponseEntity<Any> {
        val invalidRequestErrorCode = ErrorCode.BAD_REQUEST
        return ResponseEntity
            .status(httpStatus)
            .body(ErrorResponse.of(invalidRequestErrorCode, errorMessage))
    }
}
