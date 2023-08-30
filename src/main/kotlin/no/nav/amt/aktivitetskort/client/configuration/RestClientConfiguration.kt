package no.nav.amt.aktivitetskort.client.configuration

import no.nav.amt.aktivitetskort.client.AktivitetArenaAclClient
import no.nav.amt.aktivitetskort.client.AmtArenaAclClient
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableJwtTokenValidation
class RestClientConfiguration(
	@Value("\${amt.arena-acl.url}") private val amtArenaAclUrl: String,
	@Value("\${amt.arena-acl.scope}") private val amtArenaAclscope: String,
	@Value("\${aktivitet.arena-acl.url}") private val aktivitetArenaAclUrl: String,
	@Value("\${aktivitet.arena-acl.scope}") private val aktivitetArenaAclscope: String,
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
		baseUrl = amtArenaAclUrl,
		tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(amtArenaAclscope) },
	)

	@Bean
	fun aktivitetArenaAclClient(machineToMachineTokenClient: MachineToMachineTokenClient) = AktivitetArenaAclClient(
		baseUrl = aktivitetArenaAclUrl,
		tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(aktivitetArenaAclscope) },
	)
}
