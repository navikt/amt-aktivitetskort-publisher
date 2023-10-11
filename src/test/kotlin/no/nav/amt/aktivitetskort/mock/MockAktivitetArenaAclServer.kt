package no.nav.amt.aktivitetskort.mock

import no.nav.amt.aktivitetskort.client.AktivitetArenaAclClient
import no.nav.amt.aktivitetskort.utils.JsonUtils.toJsonString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.UUID

class MockAktivitetArenaAclServer : MockHttpServer("aktivitet-arena-acl-server") {
	fun addAktivitetsIdResponse(arenaId: Long, aktivitetId: UUID?) {
		val request = toJsonString(AktivitetArenaAclClient.HentAktivitetIdRequest(arenaId))

		val predicate = { req: RecordedRequest ->
			req.path == "/api/translation/arenaid" &&
				req.method == "POST" &&
				req.getBodyAsString() == request
		}

		if (aktivitetId == null) {
			addResponseHandler(predicate, MockResponse().setResponseCode(404))
		} else {
			addResponseHandler(
				predicate,
				MockResponse().setResponseCode(200).setBody(
					"""
					"$aktivitetId"
					""".trimIndent(),
				),
			)
		}
	}
}
