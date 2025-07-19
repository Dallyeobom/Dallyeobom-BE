package kr.dallyeobom.util.lock

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.redisson.api.RedissonClient
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component

@Aspect
@Component
class RedisLockAspect(
    private val redissonClient: RedissonClient,
) {
    @Around("@annotation(redisLock)")
    fun around(
        joinPoint: ProceedingJoinPoint,
        redisLock: RedisLock,
    ): Any? {
        val lockKey = getLockKey(joinPoint, redisLock)
        val lock = redissonClient.getLock(lockKey)
        var acquired = false
        try {
            acquired = lock.tryLock(redisLock.waitTime, redisLock.leaseTime, redisLock.timeUnit)
            check(acquired) { "Lock not acquired: $lockKey" }
            return joinPoint.proceed()
        } finally {
            if (acquired && lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }

    private fun getLockKey(
        joinPoint: ProceedingJoinPoint,
        redisLock: RedisLock,
    ): String {
        val signature: MethodSignature = joinPoint.signature as MethodSignature
        val context = StandardEvaluationContext()
        val paramNames: Array<String> = signature.parameterNames
        val args = joinPoint.args
        for (i in paramNames.indices) {
            context.setVariable(paramNames[i], args[i])
        }
        val parser: ExpressionParser = SpelExpressionParser()
        val dynamicKey: String =
            // annotation에서 nullable한 파라메터를 만들수 없고 parseExpression은 빈 문자열이 들어오면 에러를 반환하기 때문에 키로 사용할 값이 없다면 빈문자열을 사용하도록 함
            (if (redisLock.key.isNotBlank()) parser.parseExpression(redisLock.key).getValue(context, String::class.java) else null) ?: ""
        return redisLock.prefix + ":" + dynamicKey
    }
}
