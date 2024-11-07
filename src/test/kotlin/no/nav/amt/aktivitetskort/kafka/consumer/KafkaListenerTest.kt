package no.nav.amt.aktivitetskort.kafka.consumer

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.aktivitetskort.database.TestData.toDto
import no.nav.amt.aktivitetskort.database.TestDatabaseService
import no.nav.amt.aktivitetskort.domain.AktivitetStatus
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.utils.AsyncUtils
import no.nav.amt.aktivitetskort.utils.JsonUtils
import no.nav.amt.aktivitetskort.utils.shouldBeCloseTo
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.util.concurrent.TimeUnit

class KafkaListenerTest : IntegrationTest() {
	@Autowired
	lateinit var kafkaProducer: KafkaProducer<String, String?>

	@Autowired
	lateinit var db: TestDatabaseService

	private val offset: Long = 0

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
	fun `listen - melding om ny deltaker - deltaker upsertes og aktivitetskort opprettes`() {
		val ctx = TestData.MockContext()
		db.arrangorRepository.upsert(ctx.arrangor)
		db.deltakerlisteRepository.upsert(ctx.deltakerliste)

		mockAmtArenaAclServer.addArenaIdResponse(ctx.deltaker.id, 1234)
		mockAktivitetArenaAclServer.addAktivitetsIdResponse(1234, ctx.aktivitetskort.id)

		kafkaProducer.send(
			ProducerRecord(
				DELTAKER_TOPIC,
				ctx.deltaker.id.toString(),
				JsonUtils.toJsonString(ctx.deltaker.toDto()),
			),
		)

		AsyncUtils.eventually {
			val deltaker = db.deltakerRepository.get(ctx.deltaker.id)!!
			deltaker shouldBe ctx.deltaker

			val aktivitetskort = db.meldingRepository.getByDeltakerId(deltaker.id)!!.aktivitetskort

			aktivitetskort.personident shouldBe ctx.aktivitetskort.personident
			aktivitetskort.tittel shouldBe ctx.aktivitetskort.tittel
			aktivitetskort.aktivitetStatus shouldBe ctx.aktivitetskort.aktivitetStatus
			aktivitetskort.startDato shouldBe ctx.aktivitetskort.startDato
			aktivitetskort.sluttDato shouldBe ctx.aktivitetskort.sluttDato
			aktivitetskort.beskrivelse shouldBe ctx.aktivitetskort.beskrivelse
			aktivitetskort.endretAv shouldBe ctx.aktivitetskort.endretAv
			aktivitetskort.endretTidspunkt shouldBeCloseTo ctx.aktivitetskort.endretTidspunkt
			aktivitetskort.avtaltMedNav shouldBe ctx.aktivitetskort.avtaltMedNav
			aktivitetskort.oppgave shouldBe ctx.aktivitetskort.oppgave
			aktivitetskort.handlinger shouldNotBe null
			aktivitetskort.detaljer shouldBe ctx.aktivitetskort.detaljer
			aktivitetskort.etiketter shouldBe ctx.aktivitetskort.etiketter
		}
	}

	@Test
	fun `listen - tombstone for deltaker som har aktivt aktivitetskort - deltaker slettes og aktivitetskort avbrytes`() {
		val ctx = TestData.MockContext()
		db.arrangorRepository.upsert(ctx.arrangor)
		db.deltakerlisteRepository.upsert(ctx.deltakerliste)
		db.deltakerRepository.upsert(ctx.deltaker, offset)
		db.meldingRepository.upsert(ctx.melding)

		mockAmtArenaAclServer.addArenaIdResponse(ctx.deltaker.id, 1234)
		mockAktivitetArenaAclServer.addAktivitetsIdResponse(1234, ctx.aktivitetskort.id)

		kafkaProducer.send(
			ProducerRecord(
				DELTAKER_TOPIC,
				ctx.deltaker.id.toString(),
				null,
			),
		)

		AsyncUtils.eventually {
			val aktivitetskort = db.meldingRepository.getByDeltakerId(ctx.deltaker.id)!!.aktivitetskort
			aktivitetskort.aktivitetStatus shouldBe AktivitetStatus.AVBRUTT

			db.deltakerRepository.get(ctx.deltaker.id) shouldBe null
		}
	}

	@Test
	fun `listen - tombstone for deltaker som har inaktivt aktivitetskort - deltaker slettes og aktivitetskort endres ikke`() {
		val ctx = TestData.MockContext(deltaker = TestData.deltaker(status = DeltakerStatus(DeltakerStatus.Type.HAR_SLUTTET, null)))
		db.arrangorRepository.upsert(ctx.arrangor)
		db.deltakerlisteRepository.upsert(ctx.deltakerliste)
		db.deltakerRepository.upsert(ctx.deltaker, offset)
		db.meldingRepository.upsert(ctx.melding)

		mockAmtArenaAclServer.addArenaIdResponse(ctx.deltaker.id, 1234)
		mockAktivitetArenaAclServer.addAktivitetsIdResponse(1234, ctx.aktivitetskort.id)

		kafkaProducer.send(
			ProducerRecord(
				DELTAKER_TOPIC,
				ctx.deltaker.id.toString(),
				null,
			),
		)

		AsyncUtils.eventually {
			val aktivitetskort = db.meldingRepository.getByDeltakerId(ctx.deltaker.id)!!.aktivitetskort
			aktivitetskort.aktivitetStatus shouldBe AktivitetStatus.FULLFORT

			db.deltakerRepository.get(ctx.deltaker.id) shouldBe null
		}
	}

	@Test
	fun `listen - tombstone for deltaker som ikke aktivitetskort - deltaker slettes og aktivitetskort opprettes ikke`() {
		val deltaker = TestData.deltaker(status = DeltakerStatus(DeltakerStatus.Type.PABEGYNT_REGISTRERING, null))
		val deltakerliste = TestData.deltakerliste(deltaker.deltakerlisteId)
		val arrangor = TestData.arrangor(deltakerliste.arrangorId)
		db.arrangorRepository.upsert(arrangor)
		db.deltakerlisteRepository.upsert(deltakerliste)
		db.deltakerRepository.upsert(deltaker, offset)

		kafkaProducer.send(
			ProducerRecord(
				DELTAKER_TOPIC,
				deltaker.id.toString(),
				null,
			),
		)

		AsyncUtils.eventually {
			db.deltakerRepository.get(deltaker.id) shouldBe null
			db.meldingRepository.getByDeltakerId(deltaker.id) shouldBe null
		}
	}
}
