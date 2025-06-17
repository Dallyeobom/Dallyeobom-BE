package kr.dallyeobom.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "client.tour-api")
data class TourApiProperties(
    val apiKey: String,
)
