package no.nav.amt.aktivitetskort.client

import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.database.TestData
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AmtArrangorClientTest {
	private lateinit var client: AmtArrangorClient
	private lateinit var server: MockWebServer
	private val token = "TOKEN"

	@BeforeEach
	fun setup() {
		server = MockWebServer()
		client = AmtArrangorClient(
			baseUrl = server.url("").toString().removeSuffix("/"),
			tokenProvider = { token },
		)
	}

	@AfterEach
	fun cleanup() {
		server.shutdown()
	}

	@Test
	fun `hentArrangor - arrangor finnes - parser response og returnerer arrangor`() {
		val arrangor = TestData.arrangor()
		server.enqueue(
			MockResponse().setBody(
				"""
				{
					"id": "${arrangor.id}",
					"organisasjonsnummer": "${arrangor.organisasjonsnummer}",
					"navn": "${arrangor.navn}",
					"overordnetArrangor": null
				}
				""".trimIndent(),
			),
		)

		client.hentArrangor(arrangor.organisasjonsnummer) shouldBe arrangor
	}

	@Test
	fun `hentArrangor - arrangor finnes ikke - skal ikke skje, kaster RuntimeException`() {
		server.enqueue(MockResponse().setResponseCode(404))

		assertThrows<RuntimeException> {
			client.hentArrangor("foo")
		}
	}
}
