package kr.dallyeobom.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
class JwtProperties(
    val accessKey: String,
    val refreshKey: String,
    val accessTokenExpirationTime: Long,
    val refreshTokenExpirationTime: Long,
)
