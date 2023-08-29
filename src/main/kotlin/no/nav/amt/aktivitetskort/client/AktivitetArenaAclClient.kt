package no.nav.amt.aktivitetskort.client

import no.nav.amt.aktivitetskort.utils.JsonUtils
import no.nav.common.rest.client.RestClient
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.http.MediaType
import java.util.*
import java.util.function.Supplier

/**
 *  Klient for å håndtere kall mot Aktivitet Arena ACL
 *
 *  Swagger: https://aktivitet-arena-acl.intern.dev.nav.no/internal/swagger-ui/index.html#/TranslationController/finnAktivitetsIdForArenaId
 */
class AktivitetArenaAclClient(
	private val baseUrl: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = RestClient.baseClient(),
) {

	fun getAktivitetIdForArenaId(arenaId: Long): UUID? {
		val request = okhttp3.Request.Builder()
			.url("$baseUrl/api/translation/arenaid")
			.header("Accept", MediaType.APPLICATION_JSON_VALUE)
			.header("Authorization", "Bearer " + tokenProvider.get())
			.post(HentAktivitetIdRequest(arenaId).toRequest())
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (response.code == 404) { return null }
			if (!response.isSuccessful) {
				error("Klarte ikke å hente aktivitetId for ArenaId. Status: ${response.code}")
			}

			return UUID.fromString(response.body?.string()) ?: error("Body is missing")
		}
	}

	data class HentAktivitetIdRequest(
		val arenaId: Long,
		val aktivitetKategori: String = "TILTAKSAKTIVITET",
	) {
		fun toRequest() = JsonUtils.toJsonString(this).toRequestBody()
	}
}
