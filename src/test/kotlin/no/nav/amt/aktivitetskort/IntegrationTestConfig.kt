package no.nav.amt.aktivitetskort

import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import no.nav.amt.aktivitetskort.unleash.UnleashToggle
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class IntegrationTestConfig {
	@Bean
	fun unleashClient() = FakeUnleash().apply { enableAll() }

	@Bean
	fun unleashToggle(unleash: Unleash) = UnleashToggle(unleash)
}
