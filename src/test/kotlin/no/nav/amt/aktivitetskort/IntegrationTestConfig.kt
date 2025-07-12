package no.nav.amt.aktivitetskort

import io.getunleash.FakeUnleash
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class IntegrationTestConfig {
	@Bean
	fun unleashClient() = FakeUnleash().apply { enableAll() }
}
