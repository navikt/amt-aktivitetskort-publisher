package no.nav.amt.aktivitetskort.client

import no.nav.amt.aktivitetskort.utils.JsonUtils
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.*
import java.util.function.Supplier

class AmtArenaAclClient(
	private val baseUrl: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = baseClient(),
) {

	fun getArenaIdForAmtId(amtId: UUID): String {
		val request = Request.Builder()
			.url("$baseUrl/api/translation/$amtId")
			.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.get())
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				error("Klarte ikke å hente arenaId for AmtId. Status: ${response.code}")
			}

			val body = response.body?.string() ?: error("Body is missing")

			return JsonUtils.fromJson<HentArenaIdResponse>(body).arenaId
		}
	}

	private data class HentArenaIdResponse(
		val arenaId: String,
	)
}
