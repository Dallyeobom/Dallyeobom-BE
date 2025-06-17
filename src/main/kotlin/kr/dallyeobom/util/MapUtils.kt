package kr.dallyeobom.util

import java.net.URLEncoder

fun Map<String, String>.toFormUrlEncoded(): String =
    entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, Charsets.UTF_8)}" }
