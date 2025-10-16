package no.nav.amt.aktivitetskort.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.aktivitetskort.client.AmtArrangorClient
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.aktivitetskort.database.TestData.toDto
import no.nav.amt.aktivitetskort.domain.AktivitetStatus
import no.nav.amt.aktivitetskort.domain.Aktivitetskort
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerlistePayload
import no.nav.amt.aktivitetskort.kafka.producer.AktivitetskortProducer
import no.nav.amt.aktivitetskort.repositories.ArrangorRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerlisteRepository
import no.nav.amt.aktivitetskort.repositories.TiltakstypeRepository
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID
import java.util.function.Consumer

class KafkaConsumerServiceTest {
	private val arrangorRepository = mockk<ArrangorRepository>()
	private val deltakerlisteRepository = mockk<DeltakerlisteRepository>(relaxed = true)
	private val deltakerRepository = mockk<DeltakerRepository>()
	private val aktivitetskortService = mockk<AktivitetskortService>()
	private val amtArrangorClient = mockk<AmtArrangorClient>()
	private val aktivitetskortProducer = mockk<AktivitetskortProducer>(relaxed = true)
	private val tiltakstypeRepository = mockk<TiltakstypeRepository>()
	private val transactionTemplate = mockk<TransactionTemplate>()

	private val ctx: TestData.MockContext = TestData.MockContext()

	private val offset: Long = 0

	private val kafkaConsumerService = KafkaConsumerService(
		arrangorRepository = arrangorRepository,
		deltakerlisteRepository = deltakerlisteRepository,
		deltakerRepository = deltakerRepository,
		aktivitetskortService = aktivitetskortService,
		amtArrangorClient = amtArrangorClient,
		aktivitetskortProducer = aktivitetskortProducer,
		tiltakstypeRepository = tiltakstypeRepository,
		transactionTemplate = transactionTemplate,
	)

	@BeforeEach
	fun setup() {
		clearAllMocks()

		every { transactionTemplate.executeWithoutResult(any<Consumer<TransactionStatus>>()) } answers {
			(firstArg() as Consumer<TransactionStatus>).accept(SimpleTransactionStatus())
		}
		every { tiltakstypeRepository.getByTiltakskode(any()) } returns ctx.tiltakstype
	}

	@Test
	fun `deltakerHendelse - deltaker modifisert - publiser melding`() {
		every { deltakerRepository.upsert(ctx.deltaker, offset) } returns RepositoryResult.Modified(ctx.deltaker)
		every { aktivitetskortService.lagAktivitetskort(ctx.deltaker) } returns ctx.aktivitetskort

		kafkaConsumerService.deltakerHendelse(ctx.deltaker.id, ctx.deltaker.toDto(), offset)

		verify(exactly = 1) { deltakerRepository.upsert(ctx.deltaker, offset) }
		verify(exactly = 1) { aktivitetskortService.lagAktivitetskort(ctx.deltaker) }
		verify(exactly = 1) { aktivitetskortProducer.send(ctx.aktivitetskort) }
	}

	@Test
	fun `deltakerHendelse - deltaker lagd - publiser melding`() {
		every { deltakerRepository.upsert(ctx.deltaker, offset) } returns RepositoryResult.Created(ctx.deltaker)
		every { aktivitetskortService.lagAktivitetskort(ctx.deltaker) } returns ctx.aktivitetskort

		kafkaConsumerService.deltakerHendelse(ctx.deltaker.id, ctx.deltaker.toDto(), offset)

		verify(exactly = 1) { deltakerRepository.upsert(ctx.deltaker, offset) }
		verify(exactly = 1) { aktivitetskortService.lagAktivitetskort(ctx.deltaker) }
		verify(exactly = 1) { aktivitetskortProducer.send(ctx.aktivitetskort) }
	}

	@Test
	fun `deltakerHendelse - deltaker har ingen forandring - ikke publiser melding`() {
		every { deltakerRepository.upsert(ctx.deltaker, offset) } returns RepositoryResult.NoChange()

		kafkaConsumerService.deltakerHendelse(ctx.deltaker.id, ctx.deltaker.toDto(), offset)

		verify(exactly = 1) { deltakerRepository.upsert(ctx.deltaker, offset) }
		verify(exactly = 0) { aktivitetskortService.lagAktivitetskort(ctx.deltaker) }
		verify(exactly = 0) { aktivitetskortProducer.send(any<Aktivitetskort>()) }
	}

	@Test
	fun `deltakerHendelse - deltaker finnes ikke, status feilregistrert - publiserer ikke melding`() {
		val mockDeltaker = ctx.deltaker.copy(status = DeltakerStatus(DeltakerStatus.Type.FEILREGISTRERT, null))

		every { deltakerRepository.upsert(mockDeltaker, offset) } returns RepositoryResult.NoChange()

		kafkaConsumerService.deltakerHendelse(mockDeltaker.id, mockDeltaker.toDto(), offset)

		verify(exactly = 1) { deltakerRepository.upsert(mockDeltaker, offset) }
		verify(exactly = 0) { aktivitetskortService.lagAktivitetskort(mockDeltaker) }
		verify(exactly = 0) { aktivitetskortProducer.send(any<Aktivitetskort>()) }
	}

	@Test
	fun `deltakerHendelse - deltaker finnes, status feilregistrert - publiserer melding`() {
		val mockDeltaker = ctx.deltaker.copy(status = DeltakerStatus(DeltakerStatus.Type.FEILREGISTRERT, null))
		val mockAktivitetskort = ctx.aktivitetskort.copy(aktivitetStatus = AktivitetStatus.AVBRUTT)

		every { deltakerRepository.upsert(mockDeltaker, offset) } returns RepositoryResult.Modified(mockDeltaker)
		every { aktivitetskortService.lagAktivitetskort(mockDeltaker) } returns mockAktivitetskort

		kafkaConsumerService.deltakerHendelse(mockDeltaker.id, mockDeltaker.toDto(), offset)

		verify(exactly = 1) { deltakerRepository.upsert(mockDeltaker, offset) }
		verify(exactly = 1) { aktivitetskortService.lagAktivitetskort(mockDeltaker) }
		verify(exactly = 1) { aktivitetskortProducer.send(mockAktivitetskort) }
	}

	@Nested
	inner class DeltakerlisteHendelse {
		@Test
		fun `deltakerliste modifisert - publiser melding`() {
			every { arrangorRepository.get(ctx.arrangor.organisasjonsnummer) } returns ctx.arrangor
			every { deltakerlisteRepository.upsert(ctx.deltakerliste) } returns RepositoryResult.Modified(ctx.deltakerliste)
			every { aktivitetskortService.oppdaterAktivitetskort(ctx.deltakerliste) } returns listOf(ctx.aktivitetskort)

			kafkaConsumerService.deltakerlisteHendelse(ctx.deltakerlistePayload())

			verify(exactly = 1) { deltakerlisteRepository.upsert(ctx.deltakerliste) }
			verify(exactly = 1) { aktivitetskortService.oppdaterAktivitetskort(ctx.deltakerliste) }
			verify(exactly = 1) { aktivitetskortProducer.send(listOf(ctx.aktivitetskort)) }
		}

		@Test
		fun `deltakerliste lagd - ikke publiser melding`() {
			every { arrangorRepository.get(ctx.arrangor.organisasjonsnummer) } returns ctx.arrangor
			every { deltakerlisteRepository.upsert(ctx.deltakerliste) } returns RepositoryResult.Created(ctx.deltakerliste)

			kafkaConsumerService.deltakerlisteHendelse(ctx.deltakerlistePayload())

			verify(exactly = 1) { deltakerlisteRepository.upsert(ctx.deltakerliste) }
			verify(exactly = 0) { aktivitetskortService.oppdaterAktivitetskort(ctx.deltakerliste) }
			verify(exactly = 0) { aktivitetskortProducer.send(any<List<Aktivitetskort>>()) }
		}

		@Test
		fun `deltakerliste har ingen forandring - ikke publiser melding`() {
			every { arrangorRepository.get(ctx.arrangor.organisasjonsnummer) } returns ctx.arrangor
			every { deltakerlisteRepository.upsert(ctx.deltakerliste) } returns RepositoryResult.NoChange()

			kafkaConsumerService.deltakerlisteHendelse(ctx.deltakerlistePayload())

			verify(exactly = 1) { deltakerlisteRepository.upsert(ctx.deltakerliste) }
			verify(exactly = 0) { aktivitetskortService.oppdaterAktivitetskort(ctx.deltakerliste) }
			verify(exactly = 0) { aktivitetskortProducer.send(any<List<Aktivitetskort>>()) }
		}

		@Test
		fun `tiltak er ikke stottet - skal ikke lagre deltakerliste`() {
			val arrangor = TestData.arrangor()
			val deltakerlistePayload = DeltakerlistePayload(
				id = UUID.randomUUID(),
				navn = "navn",
				tiltakstype = DeltakerlistePayload.Tiltakstype("UKJENT"),
				virksomhetsnummer = arrangor.organisasjonsnummer,
			)

			val throwable = shouldThrow<IllegalArgumentException> {
				kafkaConsumerService.deltakerlisteHendelse(deltakerlistePayload)
			}

			throwable.message shouldBe "Tiltakskode UKJENT er ikke støttet"

			verify(exactly = 0) { arrangorRepository.get(arrangor.organisasjonsnummer) }
			verify(exactly = 0) { deltakerlisteRepository.upsert(any()) }
		}

		@Test
		fun `arrangor er ikke lagret - skal hente arrangor fra amt-arrangor`() {
			val arrangor = TestData.arrangor()
			val deltakerliste =
				TestData.deltakerliste(tiltak = Tiltak("Oppfølging", Tiltakskode.OPPFOLGING), arrangorId = arrangor.id)

			every { arrangorRepository.get(arrangor.organisasjonsnummer) } returns null andThen arrangor
			every { arrangorRepository.upsert(any()) } returns RepositoryResult.Created(arrangor)
			every { amtArrangorClient.hentArrangor(arrangor.organisasjonsnummer) } returns AmtArrangorClient.ArrangorMedOverordnetArrangorDto(
				arrangor.id,
				arrangor.navn,
				arrangor.organisasjonsnummer,
				null,
			)
			every { deltakerlisteRepository.upsert(deltakerliste) } returns RepositoryResult.Created(deltakerliste)

			kafkaConsumerService.deltakerlisteHendelse(deltakerliste.toDto(arrangor))

			verify(exactly = 2) { arrangorRepository.get(arrangor.organisasjonsnummer) }
			verify(exactly = 1) { amtArrangorClient.hentArrangor(arrangor.organisasjonsnummer) }
			verify(exactly = 1) { deltakerlisteRepository.upsert(deltakerliste) }
		}
	}

	@Test
	fun `arrangorHendelse - arrangor modifisert - publiser melding`() {
		every { arrangorRepository.upsert(ctx.arrangor) } returns RepositoryResult.Modified(ctx.arrangor)
		every { aktivitetskortService.oppdaterAktivitetskort(ctx.arrangor) } returns listOf(ctx.aktivitetskort)

		kafkaConsumerService.arrangorHendelse(ctx.arrangor.id, ctx.arrangor.toDto())

		verify(exactly = 1) { arrangorRepository.upsert(ctx.arrangor) }
		verify(exactly = 1) { aktivitetskortService.oppdaterAktivitetskort(ctx.arrangor) }
		verify(exactly = 1) { aktivitetskortProducer.send(listOf(ctx.aktivitetskort)) }
	}

	@Test
	fun `arrangorHendelse - arrangor lagd - ikke publiser melding`() {
		every { arrangorRepository.upsert(ctx.arrangor) } returns RepositoryResult.Created(ctx.arrangor)

		kafkaConsumerService.arrangorHendelse(ctx.arrangor.id, ctx.arrangor.toDto())

		verify(exactly = 1) { arrangorRepository.upsert(ctx.arrangor) }
		verify(exactly = 0) { aktivitetskortService.oppdaterAktivitetskort(ctx.arrangor) }
		verify(exactly = 0) { aktivitetskortProducer.send(any<List<Aktivitetskort>>()) }
	}

	@Test
	fun `arrangorHendelse - arrangor har ingen forandring - ikke publiser melding`() {
		every { arrangorRepository.upsert(ctx.arrangor) } returns RepositoryResult.NoChange()

		kafkaConsumerService.arrangorHendelse(ctx.arrangor.id, ctx.arrangor.toDto())

		verify(exactly = 1) { arrangorRepository.upsert(ctx.arrangor) }
		verify(exactly = 0) { aktivitetskortService.oppdaterAktivitetskort(ctx.arrangor) }
		verify(exactly = 0) { aktivitetskortProducer.send(any<List<Aktivitetskort>>()) }
	}
}
