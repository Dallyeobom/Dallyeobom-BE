package kr.dallyeobom.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("firebase")
data class FirebaseProperties(
    val firebaseServiceAccountJson: String,
)
