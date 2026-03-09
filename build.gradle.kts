import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	val kotlinVersion = "2.3.10"
	id("org.springframework.boot") version "4.0.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
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

val logstashEncoderVersion = "9.0"
val okHttpVersion = "5.3.2"
val kafkaClientsVersion = "4.2.0"
val kotestVersion = "6.1.3"
val klintVersion = "1.4.1"
val mockkVersion = "1.14.9"
val commonVersion = "3.2026.03.03_07.58-86d37775258a"
val tokenSupportVersion = "6.0.1"
val unleashVersion = "12.1.2"
val amtLibVersion = "1.2026.03.09_08.53-79eac2a4df2a"
val jacksonModuleKotlinVersion = "3.1.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-logging")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-configuration-processor")
	implementation("org.springframework.boot:spring-boot-flyway")
	implementation("org.springframework.boot:spring-boot-kafka")

	implementation("tools.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")

	implementation("org.apache.kafka:kafka-clients:$kafkaClientsVersion")

	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.postgresql:postgresql")

	implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")

	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

	implementation("no.nav.common:token-client:$commonVersion")
	implementation("no.nav.common:rest:$commonVersion")
	implementation("no.nav.common:log:$commonVersion")

	implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")

	implementation("io.getunleash:unleash-client-java:$unleashVersion")

	implementation("no.nav.amt.deltakelser.lib:models:$amtLibVersion")
	implementation("no.nav.amt.deltakelser.lib:utils:$amtLibVersion")

	testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
	testImplementation("io.kotest:kotest-assertions-json-jvm:$kotestVersion")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude("com.vaadin.external.google", "android-json")
	}
	testImplementation("org.springframework.boot:spring-boot-data-jdbc-test")
	testImplementation("org.springframework.boot:spring-boot-resttestclient")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")

	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("org.testcontainers:testcontainers-kafka")

	testImplementation("io.mockk:mockk-jvm:$mockkVersion")
	testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")

	testImplementation("no.nav.amt.deltakelser.lib:kafka:$amtLibVersion")
}

kotlin {
	jvmToolchain(25)
	compilerOptions {
		jvmTarget = JvmTarget.JVM_25
		freeCompilerArgs.addAll(
			"-Xjsr305=strict",
			"-Xannotation-default-target=param-property",
			"-Xwarning-level=IDENTITY_SENSITIVE_OPERATIONS_WITH_VALUE_TYPE:disabled",
			"-Xmulti-dollar-interpolation",
		)
	}
}

ktlint {
	version = klintVersion
}

tasks.named<Jar>("jar") {
	enabled = false
}

tasks.named<Test>("test") {
	useJUnitPlatform()
	jvmArgs(
		"-Xshare:off",
		"-XX:+EnableDynamicAgentLoading",
	)
}
