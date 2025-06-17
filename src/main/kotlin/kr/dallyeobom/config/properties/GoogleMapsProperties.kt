package kr.dallyeobom.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "client.google-maps")
data class GoogleMapsProperties(
    val apiKey: String,
)
