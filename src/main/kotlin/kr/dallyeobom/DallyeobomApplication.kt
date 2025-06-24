package kr.dallyeobom

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@ConfigurationPropertiesScan
@EnableScheduling
@Configuration
@EnableJpaAuditing
@SpringBootApplication
class DallyeobomApplication

fun main(args: Array<String>) {
    runApplication<DallyeobomApplication>(*args)
}
