package no.nav.amt.aktivitetskort.unleash

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class UnleashConfig {
	@Bean
	@Profile("default")
	fun unleashClient(
		@Value("\${app.env.unleashUrl}") unleashUrl: String,
		@Value("\${app.env.unleashApiToken}") unleashApiToken: String,
	): Unleash {
		val appName = "amt-aktivitetskort-publisher"
		val config = UnleashConfig
			.builder()
			.appName(appName)
			.instanceId(appName)
			.unleashAPI(unleashUrl)
			.apiKey(unleashApiToken)
			.build()
		return DefaultUnleash(config)
	}
}
