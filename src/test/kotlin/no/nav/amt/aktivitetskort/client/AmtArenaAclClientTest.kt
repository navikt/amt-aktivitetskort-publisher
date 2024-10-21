package no.nav.amt.aktivitetskort.client

import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class AmtArenaAclClientTest {
	private lateinit var client: AmtArenaAclClient
	private lateinit var server: MockWebServer
	private val token = "TOKEN"

	@BeforeEach
	fun setup() {
		server = MockWebServer()
		client = AmtArenaAclClient(
			baseUrl = server.url("").toString().removeSuffix("/"),
			tokenProvider = { token },
		)
	}

	@AfterEach
	fun cleanup() {
		server.shutdown()
	}

	@Test
	fun `getArenaIdForAmtId - returnerer arenaid om eksisterer`() {
		val arenaId = 1L
		val response = """{"arenaId": "$arenaId"}, "arenaHistId": null}""""
		server.enqueue(MockResponse().setBody(response))

		val id = client.getArenaIdForAmtId(UUID.randomUUID())

		id shouldBe arenaId
	}

	@Test
	fun `getArenaIdForAmtId - returnerer null om arenahistid finnes`() {
		val arenaHistId = 1L
		val response = """{"arenaId": null}, "arenaHistId": "$arenaHistId"}""""
		server.enqueue(MockResponse().setBody(response))

		client.getArenaIdForAmtId(UUID.randomUUID()) shouldBe null
	}

	@Test
	fun `getArenaIdForAmtId - returnerer null om ingen arenaid finnes`() {
		val response = """{"arenaId": null}, "arenaHistId": null}""""
		server.enqueue(MockResponse().setBody(response))

		client.getArenaIdForAmtId(UUID.randomUUID()) shouldBe null
	}

	@Test
	fun `getArenaIdForAmtId - kaster feil hvis respons er 404`() {
		server.enqueue(MockResponse().setResponseCode(404))

		assertThrows<IllegalStateException> {
			client.getArenaIdForAmtId(UUID.randomUUID())
		}
	}
}
