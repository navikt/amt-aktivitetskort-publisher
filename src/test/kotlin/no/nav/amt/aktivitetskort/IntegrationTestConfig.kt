package no.nav.amt.aktivitetskort

import io.getunleash.FakeUnleash
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("test")
@Configuration
class IntegrationTestConfig {
	@Bean
	fun unleashClient(): FakeUnleash {
		val fakeUnleash = FakeUnleash()
		fakeUnleash.enableAll()
		return fakeUnleash
	}
}
