package kr.dallyeobom

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@ConfigurationPropertiesScan
@Configuration
@EnableJpaAuditing
@SpringBootApplication
class DallyeobomApplication

fun main(args: Array<String>) {
    runApplication<DallyeobomApplication>(*args)
}
