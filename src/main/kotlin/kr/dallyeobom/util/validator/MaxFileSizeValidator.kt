package kr.dallyeobom.util.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.springframework.web.multipart.MultipartFile
import kotlin.reflect.KClass

@Constraint(validatedBy = [MaxFileSizeValidator::class, MaxFileSizeListValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MaxFileSize(
    val max: Int = 1 * 1024 * 1024,
    val message: String = "파일 사이즈가 초과하였습니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class MaxFileSizeValidator : ConstraintValidator<MaxFileSize, MultipartFile?> {
    private var maxFileSize: Int = 0

    override fun initialize(constraintAnnotation: MaxFileSize) {
        this.maxFileSize = constraintAnnotation.max
    }

    override fun isValid(
        file: MultipartFile?,
        context: ConstraintValidatorContext,
    ): Boolean = file == null || file.size <= maxFileSize
}

class MaxFileSizeListValidator : ConstraintValidator<MaxFileSize, List<MultipartFile>?> {
    private var maxFileSize: Int = 0

    override fun initialize(constraintAnnotation: MaxFileSize) {
        this.maxFileSize = constraintAnnotation.max
    }

    override fun isValid(
        files: List<MultipartFile>?,
        context: ConstraintValidatorContext,
    ): Boolean = files.isNullOrEmpty() || files.all { it.size <= maxFileSize }
}
