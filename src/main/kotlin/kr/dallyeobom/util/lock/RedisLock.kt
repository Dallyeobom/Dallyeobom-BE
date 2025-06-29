package kr.dallyeobom.util.lock

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RedisLock(
    val prefix: String, // 락 키 prefix
    val key: String, // SpEL 표현식: "#paramName"
    val waitTime: Long,
    val leaseTime: Long,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
)
