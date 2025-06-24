package kr.dallyeobom.config

import kr.dallyeobom.config.properties.RedissonProperties
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.config.Protocol
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedissonClientConfig {
    @Bean
    fun redissonClient(redissonConfig: RedissonProperties): RedissonClient {
        val config = Config()
        config.setProtocol(Protocol.RESP3) // Client side caching을 위한 설정
        config
            .useSingleServer()
            .setAddress("redis://${redissonConfig.host}:${redissonConfig.port}")
            .setPassword(redissonConfig.password)
        return Redisson.create(config)
    }
}
