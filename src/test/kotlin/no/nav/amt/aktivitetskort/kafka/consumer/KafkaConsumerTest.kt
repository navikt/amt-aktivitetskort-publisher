package no.nav.amt.aktivitetskort.kafka.consumer

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.aktivitetskort.database.TestData.toDto
import no.nav.amt.aktivitetskort.domain.AktivitetStatus
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.utils.AsyncUtils
import no.nav.amt.aktivitetskort.utils.JsonUtils
import no.nav.amt.aktivitetskort.utils.shouldBeCloseTo
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.TimeUnit

class KafkaConsumerTest(
	private val kafkaProducer: KafkaProducer<String, String?>,
) : IntegrationTest() {
	private val offset: Long = 0

	@BeforeEach
	fun setup() {
		mockAktivitetArenaAclServer.clearResponses()
	}

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
			testDatabase.arrangorRepository.get(arrangor.id) != null
		}
	}

	@Test
	fun `listen - melding om ny deltakerliste - deltakerliste upsertes`() {
		val ctx = TestData.MockContext()
		testDatabase.arrangorRepository.upsert(ctx.arrangor)

		kafkaProducer.send(
			ProducerRecord(
				DELTAKERLISTE_TOPIC,
				ctx.deltakerliste.id.toString(),
				JsonUtils.toJsonString(ctx.deltakerlisteDto()),
			),
		)

		Awaitility.await().atMost(5, TimeUnit.SECONDS).until {
			testDatabase.deltakerlisteRepository.get(ctx.deltakerliste.id) != null
		}
	}

	@Test
	fun `listen - melding om ny deltaker - deltaker upsertes og aktivitetskort opprettes`() {
		val ctx = TestData.MockContext()
		testDatabase.arrangorRepository.upsert(ctx.arrangor)
		testDatabase.deltakerlisteRepository.upsert(ctx.deltakerliste)

		mockAmtArenaAclServer.addArenaIdResponse(ctx.deltaker.id, 1234)
		mockAktivitetArenaAclServer.addAktivitetsIdResponse(1234, ctx.melding.id)
		mockVeilarboppfolgingServer.addResponse()

		kafkaProducer.send(
			ProducerRecord(
				DELTAKER_TOPIC,
				ctx.deltaker.id.toString(),
				JsonUtils.toJsonString(ctx.deltaker.toDto()),
			),
		)

		AsyncUtils.eventually {
			ctx.melding.id shouldBe ctx.aktivitetskortId
			ctx.melding.aktivitetskort.id shouldBe ctx.aktivitetskortId
			val deltaker = testDatabase.deltakerRepository.get(ctx.deltaker.id)!!
			deltaker shouldBe ctx.deltaker

			val aktivitetskort = testDatabase.meldingRepository
				.getByDeltakerId(deltaker.id)
				.first()
				.aktivitetskort

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
	fun `listen - melding om oppdatert deltaker, dab sender ny id - nytt aktivitetskort opprettes med mottatt id`() {
		val ctx = TestData.MockContext(oppfolgingsperiodeId = null)
		val endretDeltaker = ctx.deltaker.copy(
			sluttdato = LocalDate.now().plusDays(1),
		)
		val nyId = UUID.randomUUID()
		testDatabase.arrangorRepository.upsert(ctx.arrangor)
		testDatabase.deltakerlisteRepository.upsert(ctx.deltakerliste)
		testDatabase.deltakerRepository.upsert(ctx.deltaker, -1)
		testDatabase.meldingRepository.upsert(ctx.melding)

		mockAmtArenaAclServer.addArenaIdResponse(ctx.deltaker.id, 1234)
		mockAktivitetArenaAclServer.addAktivitetsIdResponse(1234, nyId)
		mockVeilarboppfolgingServer.addResponse()

		kafkaProducer.send(
			ProducerRecord(
				DELTAKER_TOPIC,
				ctx.deltaker.id.toString(),
				JsonUtils.toJsonString(endretDeltaker.toDto()),
			),
		)

		AsyncUtils.eventually {
			val deltaker = testDatabase.deltakerRepository.get(ctx.deltaker.id)!!
			deltaker shouldBe endretDeltaker

			val aktivitetskort = testDatabase.meldingRepository
				.getByDeltakerId(deltaker.id)

			aktivitetskort.size shouldBe 2
			val nyttAktivitetskort = aktivitetskort
				.first()
				.aktivitetskort
			nyttAktivitetskort.id shouldBe nyId
			nyttAktivitetskort shouldBe ctx.aktivitetskort.copy(
				id = nyId,
				sluttDato = endretDeltaker.sluttdato,
			)
		}
	}

	@Test
	fun `listen - tombstone for deltaker som har aktivt aktivitetskort - deltaker slettes og aktivitetskort avbrytes`() {
		val ctx = TestData.MockContext(oppfolgingsperiodeId = UUID.randomUUID())
		testDatabase.arrangorRepository.upsert(ctx.arrangor)
		testDatabase.deltakerlisteRepository.upsert(ctx.deltakerliste)
		testDatabase.deltakerRepository.upsert(ctx.deltaker, offset)
		testDatabase.insertAktivOppfolgingsperiode(id = ctx.oppfolgingsperiodeId!!)
		testDatabase.meldingRepository.upsert(ctx.melding)

		mockAmtArenaAclServer.addArenaIdResponse(ctx.deltaker.id, 1234)
		mockAktivitetArenaAclServer.addAktivitetsIdResponse(1234, ctx.aktivitetskort.id)
		mockVeilarboppfolgingServer.addResponse()

		kafkaProducer.send(
			ProducerRecord(
				DELTAKER_TOPIC,
				ctx.deltaker.id.toString(),
				null,
			),
		)

		AsyncUtils.eventually {
			val aktivitetskort = testDatabase.meldingRepository
				.getByDeltakerId(ctx.deltaker.id)
				.first()
				.aktivitetskort
			aktivitetskort.aktivitetStatus shouldBe AktivitetStatus.AVBRUTT

			testDatabase.deltakerRepository.get(ctx.deltaker.id) shouldBe null
		}
	}

	@Test
	fun `listen - tombstone for deltaker som har inaktivt aktivitetskort - deltaker slettes og aktivitetskort endres ikke`() {
		val ctx = TestData.MockContext(
			oppfolgingsperiodeId = UUID.randomUUID(),
			deltaker = TestData.deltaker(status = DeltakerStatus(DeltakerStatus.Type.HAR_SLUTTET, null)),
		)
		testDatabase.arrangorRepository.upsert(ctx.arrangor)
		testDatabase.deltakerlisteRepository.upsert(ctx.deltakerliste)
		testDatabase.deltakerRepository.upsert(ctx.deltaker, offset)
		testDatabase.insertAktivOppfolgingsperiode(id = ctx.oppfolgingsperiodeId!!)
		testDatabase.meldingRepository.upsert(ctx.melding)

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
			val aktivitetskort = testDatabase.meldingRepository
				.getByDeltakerId(ctx.deltaker.id)
				.firstOrNull()!!
				.aktivitetskort
			aktivitetskort.aktivitetStatus shouldBe AktivitetStatus.FULLFORT

			testDatabase.deltakerRepository.get(ctx.deltaker.id) shouldBe null
		}
	}

	@Test
	fun `listen - tombstone for deltaker som ikke aktivitetskort - deltaker slettes og aktivitetskort opprettes ikke`() {
		val deltaker = TestData.deltaker(status = DeltakerStatus(DeltakerStatus.Type.PABEGYNT_REGISTRERING, null))
		val deltakerliste = TestData.deltakerliste(deltaker.deltakerlisteId)
		val arrangor = TestData.arrangor(deltakerliste.arrangorId)
		testDatabase.arrangorRepository.upsert(arrangor)
		testDatabase.deltakerlisteRepository.upsert(deltakerliste)
		testDatabase.deltakerRepository.upsert(deltaker, offset)

		kafkaProducer.send(
			ProducerRecord(
				DELTAKER_TOPIC,
				deltaker.id.toString(),
				null,
			),
		)

		AsyncUtils.eventually {
			testDatabase.deltakerRepository.get(deltaker.id) shouldBe null
			testDatabase.meldingRepository.getByDeltakerId(deltaker.id) shouldBe emptyList()
		}
	}
}
