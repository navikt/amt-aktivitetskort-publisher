package no.nav.amt.aktivitetskort.kafka.consumer

import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.aktivitetskort.database.TestData.toDto
import no.nav.amt.aktivitetskort.database.TestDatabaseService
import no.nav.amt.aktivitetskort.utils.JsonUtils
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.util.concurrent.TimeUnit

class KafkaListenerTest : IntegrationTest() {

	@Autowired
	lateinit var kafkaProducer: KafkaProducer<String, String>

	@Autowired
	lateinit var db: TestDatabaseService

	@Test
	fun `listen - melding om ny arrangor - arrangor upsertes`() {
		val arrangor = TestData.arrangor()

		kafkaProducer.send(
			ProducerRecord(
				ARRANGOR_TOPIC,
				arrangor.id.toString(),
				JsonUtils.toJsonString(arrangor.toDto()),
			),
		)

		Awaitility.await().atMost(5, TimeUnit.SECONDS).until {
			db.arrangorRepository.get(arrangor.id) != null
		}
	}

	@Test
	fun `listen - melding om ny deltakerliste - deltakerliste upsertes`() {
		val ctx = TestData.MockContext()
		db.arrangorRepository.upsert(ctx.arrangor)

		kafkaProducer.send(
			ProducerRecord(
				DELTAKERLISTE_TOPIC,
				ctx.deltakerliste.id.toString(),
				JsonUtils.toJsonString(ctx.deltakerlisteDto()),
			),
		)

		Awaitility.await().atMost(5, TimeUnit.SECONDS).until {
			db.deltakerlisteRepository.get(ctx.deltakerliste.id) != null
		}
	}

	@Test
	fun `listen - melding om ny deltaker - deltaker upsertes`() {
		val ctx = TestData.MockContext()
		db.arrangorRepository.upsert(ctx.arrangor)
		db.deltakerlisteRepository.upsert(ctx.deltakerliste)

		kafkaProducer.send(
			ProducerRecord(
				DELTAKER_TOPIC,
				ctx.deltaker.id.toString(),
				JsonUtils.toJsonString(ctx.deltaker.toDto()),
			),
		)

		Awaitility.await().atMost(5, TimeUnit.SECONDS).until {
			db.deltakerRepository.get(ctx.deltaker.id) != null
		}
	}
}
