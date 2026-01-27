plugins {
	val kotlinVersion = "2.3.0"
	id("org.springframework.boot") version "3.5.7"
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
val kafkaClientsVersion = "4.1.1"
val kotestVersion = "6.0.7"
val testcontainersVersion = "2.0.3"
val klintVersion = "1.4.1"
val mockkVersion = "1.14.7"
val commonVersion = "3.2025.10.10_08.21-bb7c7830d93c"
val tokenSupportVersion = "5.0.39"
val unleashVersion = "12.0.1"
val amtLibVersion = "1.2025.12.12_11.03-2e0d1281814d"

// fjernes ved neste release av org.apache.kafka:kafka-clients
configurations.configureEach {
	resolutionStrategy {
		capabilitiesResolution {
			withCapability("org.lz4:lz4-java") {
				select(candidates.first { (it.id as ModuleComponentIdentifier).group == "at.yawk.lz4" })
			}
		}
	}
}

dependencyManagement {
	imports {
		mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
	}
}

dependencies {
	implementation("at.yawk.lz4:lz4-java:1.10.2") // fjernes ved neste release av org.apache.kafka:kafka-clients
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

	implementation("no.nav.amt.lib:models:$amtLibVersion")
	implementation("no.nav.amt.lib:utils:$amtLibVersion")

	testImplementation(kotlin("test"))
	testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
	testImplementation("io.kotest:kotest-assertions-json-jvm:$kotestVersion")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude("com.vaadin.external.google", "android-json")
	}

	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.testcontainers:kafka")
	testImplementation("io.mockk:mockk-jvm:$mockkVersion")
	testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
}

kotlin {
	jvmToolchain(21)
	compilerOptions {
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

tasks.jar {
	enabled = false
}

tasks.test {
	useJUnitPlatform()
	jvmArgs(
		"-Xshare:off",
		"-XX:+EnableDynamicAgentLoading",
	)
}
