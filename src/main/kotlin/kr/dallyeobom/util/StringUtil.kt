package kr.dallyeobom.util

fun String.subStringOrReturn(limit: Int): String =
    if (this.length > limit) {
        this.substring(0, limit)
    } else {
        this
    }
