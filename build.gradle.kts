plugins {
	id("org.springframework.boot") version "3.1.3"
	id("io.spring.dependency-management") version "1.1.3"
	id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
	kotlin("plugin.spring") version "1.9.10"
	kotlin("jvm") version "1.9.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
	maven { setUrl("https://jitpack.io") }
	maven { setUrl("https://packages.confluent.io/maven/") }
}

val logstashEncoderVersion = "7.4"
val okHttpVersion = "4.11.0"
val kafkaClientsVersion = "3.5.1"
val kotestVersion = "5.7.2"
val testcontainersVersion = "1.19.0"
val klintVersion = "0.49.1"
val mockkVersion = "1.13.7"
val commonVersion = "3.2023.07.07_09.10-85326e9557f0"
val tokenSupportVersion = "3.1.5"
val mockOauth2ServerVersion = "1.0.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-logging")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-configuration-processor")

	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.apache.kafka:kafka-clients:$kafkaClientsVersion")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	implementation("org.flywaydb:flyway-core")
	implementation("org.postgresql:postgresql")

	implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")

	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

	implementation("com.github.navikt.common-java-modules:token-client:$commonVersion")
	implementation("com.github.navikt.common-java-modules:rest:$commonVersion")

	implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")

	testImplementation(kotlin("test"))
	testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude("com.vaadin.external.google", "android-json")
	}

	testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
	testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
	testImplementation("org.testcontainers:kafka:$testcontainersVersion")
	testImplementation("io.mockk:mockk:$mockkVersion")
	testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
	testImplementation("no.nav.security:mock-oauth2-server:$mockOauth2ServerVersion")
}

tasks.test {
	useJUnitPlatform()
}

kotlin {
	jvmToolchain(17)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
	version.set(klintVersion)
}
