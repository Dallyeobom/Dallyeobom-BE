plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.spring") version "2.1.21"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.1.21"
    id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
    id("com.google.cloud.tools.jib") version "3.4.5"
}

group = "kr"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

ktlint {
    version.set("1.6.0")
}

jib {
    from {
        image = "eclipse-temurin:21-jre"
        platforms {
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        image = "ghcr.io/dallyeobom/dallyeobom_be:latest"
        auth {
            username = System.getenv("DOCKER_USERNAME") ?: ""
            password = System.getenv("DOCKER_PASSWORD") ?: ""
        }
    }
    container {
        environment = mapOf("TZ" to "Asia/Seoul")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.hibernate.orm:hibernate-spatial:6.6.15.Final")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // Secret & Config
    implementation("io.awspring.cloud:spring-cloud-aws-dependencies:3.3.0")
    implementation("io.awspring.cloud:spring-cloud-aws-starter-parameter-store:3.3.0")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap:4.2.1")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Infrastructure
    implementation("com.oracle.database.jdbc:ojdbc11:23.8.0.25.04")
    implementation("org.redisson:redisson-spring-boot-starter:3.47.0")
    // implementation("org.hibernate.search:hibernate-search-backend-elasticsearch:7.2.3.Final") // OpenSearch를 사용하게되면 주석 해제

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
