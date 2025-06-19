package kr.dallyeobom.util

fun requireNull(
    value: Any?,
    lazyMessage: () -> Any,
) {
    if (value != null) {
        throw IllegalArgumentException(lazyMessage().toString())
    }
}
