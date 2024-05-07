plugins {
	val kotlinVersion = "1.9.23"

	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.5"
	id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
	kotlin("plugin.spring") version kotlinVersion
	kotlin("jvm") version kotlinVersion
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
	maven { setUrl("https://github-package-registry-mirror.gc.nav.no/cached/maven-release") }
	maven { setUrl("https://packages.confluent.io/maven/") }
}

val logstashEncoderVersion = "7.4"
val okHttpVersion = "4.12.0"
val kafkaClientsVersion = "3.7.0"
val kotestVersion = "5.8.1"
val testcontainersVersion = "1.19.7"
val klintVersion = "1.2.1"
val mockkVersion = "1.13.10"
val commonVersion = "3.2024.04.22_13.50-7815154a2573"
val tokenSupportVersion = "4.1.4"
val unleashVersion = "9.2.1"

extra["postgresql.version"] = "42.7.2"

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

	implementation("no.nav.common:token-client:$commonVersion")
	implementation("no.nav.common:rest:$commonVersion")
	implementation("no.nav.common:log:$commonVersion")

	implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")

	implementation("io.getunleash:unleash-client-java:$unleashVersion")

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
}

tasks.test {
	useJUnitPlatform()
}

kotlin {
	jvmToolchain(21)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
	version.set(klintVersion)
}
