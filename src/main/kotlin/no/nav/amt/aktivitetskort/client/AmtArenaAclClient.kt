package no.nav.amt.aktivitetskort.client

import no.nav.amt.aktivitetskort.utils.JsonUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AmtArenaAclClient(
	@Value("\${amt.arena-acl.url}") private val baseUrl: String,
	private val amtArenaAclClientOkHttpClient: OkHttpClient,
) {

	fun getArenaIdForAmtId(amtId: UUID): String {
		val request = Request.Builder()
			.url("$baseUrl/api/translation/$amtId")
			.get()
			.build()

		amtArenaAclClientOkHttpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente arenaId for AmtId. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			return JsonUtils.fromJson<HentArenaIdResponse>(body).arenaId
		}
	}

	private data class HentArenaIdResponse(
		val arenaId: String,
	)
}
