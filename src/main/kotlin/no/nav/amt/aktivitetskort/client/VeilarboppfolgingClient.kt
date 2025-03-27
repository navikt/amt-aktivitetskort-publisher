package no.nav.amt.aktivitetskort.client

import no.nav.amt.aktivitetskort.domain.Oppfolgingsperiode
import no.nav.amt.aktivitetskort.utils.JsonUtils
import no.nav.amt.aktivitetskort.utils.JsonUtils.toJsonString
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.ZonedDateTime
import java.util.UUID
import java.util.function.Supplier

class VeilarboppfolgingClient(
	private val baseUrl: String,
	private val veilarboppfolgingTokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = baseClient(),
) {
	companion object {
		private val mediaTypeJson = "application/json".toMediaType()
	}

	fun hentOppfolgingperiode(fnr: String): Oppfolgingsperiode {
		val personRequestJson = toJsonString(PersonRequest(fnr))
		val request = Request
			.Builder()
			.url("$baseUrl/api/v3/oppfolging/hent-gjeldende-periode")
			.header("Accept", "application/json; charset=utf-8")
			.header("Authorization", "Bearer ${veilarboppfolgingTokenProvider.get()}")
			.post(personRequestJson.toRequestBody(mediaTypeJson))
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Uventet status ved hent status-kall mot veilarboppfolging ${response.code}")
			}
			val body = response.body?.string() ?: throw RuntimeException("Body mangler i hent status-respons fra veilarboppfolging")

			return JsonUtils.fromJson<OppfolgingPeriodeDTO>(body).toModel()
		}
	}

	private data class PersonRequest(
		val fnr: String,
	)

	data class OppfolgingPeriodeDTO(
		val uuid: UUID,
		val startDato: ZonedDateTime,
		val sluttDato: ZonedDateTime?,
	) {
		fun toModel() = Oppfolgingsperiode(
			id = uuid,
			startDato = startDato.toLocalDateTime(),
			sluttDato = sluttDato?.toLocalDateTime(),
		)
	}
}
