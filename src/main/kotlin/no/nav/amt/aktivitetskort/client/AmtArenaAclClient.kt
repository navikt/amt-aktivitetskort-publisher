package no.nav.amt.aktivitetskort.client

import no.nav.amt.aktivitetskort.utils.JsonUtils
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.UUID
import java.util.function.Supplier

class AmtArenaAclClient(
	private val baseUrl: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = baseClient(),
) {
	private val log = LoggerFactory.getLogger(javaClass)

	fun getArenaIdForAmtId(amtId: UUID): Long? {
		val request = Request
			.Builder()
			.url("$baseUrl/api/v2/translation/$amtId")
			.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.get())
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				error("Klarte ikke å hente arenaId for AmtId $amtId. Status: ${response.code}")
			}

			val body = response.body?.string() ?: error("Body is missing")
			val hentArenaIdV2Response = JsonUtils.fromJson<HentArenaIdV2Response>(body)
			if (hentArenaIdV2Response.arenaId != null) {
				return hentArenaIdV2Response.arenaId.toLong()
			} else if (hentArenaIdV2Response.arenaHistId != null) {
				log.info("amtId $amtId tilhører histdeltaker med id ${hentArenaIdV2Response.arenaHistId}")
				return null
			} else {
				log.warn("Fant ikke arenaId eller arenaHistId for deltaker med id $amtId")
				return null
			}
		}
	}

	private data class HentArenaIdV2Response(
		val arenaId: String?,
		val arenaHistId: String?,
	)
}
