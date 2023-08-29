package no.nav.amt.aktivitetskort.client.configuration

import no.nav.amt.aktivitetskort.client.AmtArenaAclClient
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.concurrent.TimeUnit

@Configuration
@EnableJwtTokenValidation
class RestClientConfiguration(
	@Value("\${amt.arena-acl.url}") private val arenaAclUrl: String,
	@Value("\${amt.arena-acl.scope}") private val arenaAclscope: String,
) {

	@Bean
	fun machineToMachineTokenClient(
		@Value("\${nais.env.azureAppClientId}") azureAdClientId: String,
		@Value("\${nais.env.azureOpenIdConfigTokenEndpoint}") azureTokenEndpoint: String,
		@Value("\${nais.env.azureAppJWK}") azureAdJWK: String,
	): MachineToMachineTokenClient {
		return AzureAdTokenClientBuilder.builder()
			.withClientId(azureAdClientId)
			.withTokenEndpointUrl(azureTokenEndpoint)
			.withPrivateJwk(azureAdJWK)
			.buildMachineToMachineTokenClient()
	}

	@Bean
	fun amtArenaAclClient(machineToMachineTokenClient: MachineToMachineTokenClient) = AmtArenaAclClient(
		baseUrl = arenaAclUrl,
		tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(arenaAclscope) }
	)
}
