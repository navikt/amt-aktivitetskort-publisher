package no.nav.amt.aktivitetskort.client.configuration

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
	fun amtArenaAclClientOkHttpClient(
		machineToMachineTokenClient: MachineToMachineTokenClient,
		@Value("\${amt.arena-acl.scope}") scope: String,
	): OkHttpClient {
		return OkHttpClient.Builder()
			.connectTimeout(5, TimeUnit.SECONDS)
			.readTimeout(5, TimeUnit.SECONDS)
			.followRedirects(false)
			.addInterceptor(tokenInterceptor { machineToMachineTokenClient.createMachineToMachineToken(scope) })
			.build()
	}

	private fun tokenInterceptor(
		tokenProvider: () -> String,
	): Interceptor {
		return Interceptor { chain ->
			val request = chain.request()
			val newRequest = request.newBuilder()
				.addHeader("Authorization", "Bearer ${tokenProvider()}")
				.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.build()
			chain.proceed(newRequest)
		}
	}
}
