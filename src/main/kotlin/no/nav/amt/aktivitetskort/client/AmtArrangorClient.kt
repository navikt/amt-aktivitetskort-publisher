package no.nav.amt.aktivitetskort.client

import no.nav.amt.aktivitetskort.domain.Arrangor
import no.nav.amt.aktivitetskort.utils.JsonUtils
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import java.util.function.Supplier

class AmtArrangorClient(
	private val baseUrl: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = baseClient(),
) {

	fun hentArrangor(orgnummer: String): Arrangor {
		val request = Request.Builder()
			.url("$baseUrl/api/service/arrangor/organisasjonsnummer/$orgnummer")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Kunne ikke hente arrangør med orgnummer $orgnummer fra amt-arrangør. Status=${response.code}")
			}
			val body = response.body?.string() ?: throw RuntimeException("Body is missing")
			val arrangor = JsonUtils.fromJson<ArrangorDto>(body)
			return Arrangor(arrangor.id, arrangor.organisasjonsnummer, arrangor.navn)
		}
	}

	data class ArrangorDto(
		val id: UUID,
		val navn: String,
		val organisasjonsnummer: String,
	)
}
