package kr.dallyeobom.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import kr.dallyeobom.config.properties.FirebaseProperties
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream

@Configuration
class FirebaseConfig(
    private val firebaseProperties: FirebaseProperties,
) {
    @PostConstruct
    fun init() {
        if (FirebaseApp.getApps().isNotEmpty()) return

        ByteArrayInputStream(firebaseProperties.firebaseServiceAccountJson.toByteArray()).use { credentials ->
            val options =
                FirebaseOptions
                    .builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build()

            FirebaseApp.initializeApp(options)
        }
    }
}
