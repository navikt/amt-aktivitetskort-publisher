package no.nav.amt.aktivitetskort.internal

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.TestUtils.staticObjectMapper
import no.nav.amt.aktivitetskort.kafka.consumer.AKTIVITETSKORT_TOPIC
import no.nav.amt.aktivitetskort.kafka.producer.dto.AktivitetskortKasseringPayload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.readValue
import java.time.Duration
import java.util.Properties
import java.util.UUID

class InternalAPITest : IntegrationTest() {
	private fun kafkaConsumer() = KafkaConsumer<String, String>(
		Properties().apply {
			put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
			put(ConsumerConfig.GROUP_ID_CONFIG, "test-internal-api-${UUID.randomUUID()}")
			put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
			put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
			put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
		},
	).also {
		it.subscribe(listOf(AKTIVITETSKORT_TOPIC))
		it.poll(Duration.ofMillis(500)) // seek to end of topic
	}

	@Test
	fun `slettAktivitetskort - kall fra intern ip - sender kassering melding til Kafka`() {
		val aktivitetskortId = UUID.randomUUID()
		val body =
			"""
			{
				"aktivitetskortId": "$aktivitetskortId",
				"personIdent": "12345678901",
				"navIdent": "Z123456"
			}
			""".trimIndent()

		kafkaConsumer().use { consumer ->
			sendRequest("POST", "/internal/slett/", body.toRequestBody("application/json".toMediaType()))
				.code shouldBe 200

			val record = consumer
				.poll(Duration.ofSeconds(10))
				.first { it.key() == aktivitetskortId.toString() }
			val payload = staticObjectMapper.readValue<AktivitetskortKasseringPayload>(record.value())

			assertSoftly(payload) {
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
		val response = sendRequest(
			"POST",
			"/internal/slett/",
			"""{ "aktivitetskortId": "${UUID.randomUUID()}" }""".toRequestBody("application/json".toMediaType()),
		)
		response.code shouldBe 400
	}
}
