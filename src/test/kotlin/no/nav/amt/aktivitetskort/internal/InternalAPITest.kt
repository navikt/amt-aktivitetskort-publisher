package no.nav.amt.aktivitetskort.internal

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.TestUtils.staticObjectMapper
import no.nav.amt.aktivitetskort.kafka.consumer.AKTIVITETSKORT_TOPIC
import no.nav.amt.aktivitetskort.kafka.producer.dto.AktivitetskortKasseringPayload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.readValue
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit

class InternalAPITest : IntegrationTest() {
	// serverUrl() uses "localhost" which may resolve to ::1 on macOS — force IPv4 so remoteAddr == "127.0.0.1"
	private fun postInternal(path: String, body: String) = OkHttpClient()
		.newCall(
			Request
				.Builder()
				.url(serverUrl().replace("localhost", "127.0.0.1") + path)
				.post(body.toRequestBody("application/json".toMediaType()))
				.build(),
		).execute()

	private fun kafkaConsumer() = KafkaConsumer<String, String>(
		mapOf(
			ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers,
			ConsumerConfig.GROUP_ID_CONFIG to "test-internal-api-${UUID.randomUUID()}",
			ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
			ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
			ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
		),
	).also {
		it.subscribe(listOf(AKTIVITETSKORT_TOPIC))
		await().atMost(10, TimeUnit.SECONDS).until {
			it.poll(Duration.ofMillis(100))
			it.assignment().isNotEmpty()
		}
	}

	@Test
	fun `slettAktivitetskort - kall fra intern ip - sender kassering melding til Kafka`() {
		val aktivitetskortId = UUID.randomUUID()
		val body = """{"aktivitetskortId": "$aktivitetskortId", "personIdent": "12345678901", "navIdent": "Z123456"}"""

		kafkaConsumer().use { consumer ->
			postInternal("/internal/slett/", body).code shouldBe 200

			var payload: AktivitetskortKasseringPayload? = null
			await().atMost(10, TimeUnit.SECONDS).untilAsserted {
				payload = consumer
					.poll(Duration.ofMillis(200))
					.firstOrNull { it.key() == aktivitetskortId.toString() }
					?.let { staticObjectMapper.readValue(it.value()) }
				payload shouldNotBe null
			}

			assertSoftly(payload!!) {
				messageId shouldBe aktivitetskortId
				aktivitetsId shouldBe aktivitetskortId
				actionType shouldBe "KASSER_AKTIVITET"
				personIdent shouldBe "12345678901"
				navIdent shouldBe "Z123456"
			}
		}
	}

	@Test
	fun `slettAktivitetskort - manglende felt i body - returnerer 400`() {
		postInternal(
			"/internal/slett/",
			"""{ "aktivitetskortId": "${UUID.randomUUID()}" }""",
		).code shouldBe 400
	}
}
