package no.nav.amt.aktivitetskort.service

import io.getunleash.DefaultUnleash
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.aktivitetskort.client.AmtArrangorClient
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.aktivitetskort.database.TestData.toDto
import no.nav.amt.aktivitetskort.domain.AktivitetStatus
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerlisteDto
import no.nav.amt.aktivitetskort.repositories.ArrangorRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerlisteRepository
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import java.util.UUID

class HendelseServiceTest {
	private val arrangorRepository = mockk<ArrangorRepository>()
	private val deltakerlisteRepository = mockk<DeltakerlisteRepository>()
	private val deltakerRepository = mockk<DeltakerRepository>()
	private val aktivitetskortService = mockk<AktivitetskortService>()
	private val amtArrangorClient = mockk<AmtArrangorClient>()
	private val template = mockk<KafkaTemplate<String, String>>(relaxed = true)
	private val unleash = mockk<DefaultUnleash>()
	private val metricsService = mockk<MetricsService>(relaxed = true)

	private val offset: Long = 0

	private val hendelseService = HendelseService(
		arrangorRepository = arrangorRepository,
		deltakerlisteRepository = deltakerlisteRepository,
		deltakerRepository = deltakerRepository,
		aktivitetskortService = aktivitetskortService,
		amtArrangorClient = amtArrangorClient,
		template = template,
		unleash = unleash,
		metricsService = metricsService,
	)

	@BeforeEach
	fun setup() {
		every { unleash.isEnabled("amt.relast-aktivitetskort-deltaker") } returns false
	}

	@Test
	fun `deltakerHendelse - deltaker modifisert - publiser melding`() {
		val ctx = TestData.MockContext()

		every { deltakerRepository.upsert(ctx.deltaker, offset) } returns RepositoryResult.Modified(ctx.deltaker)
		every { aktivitetskortService.lagAktivitetskort(ctx.deltaker) } returns ctx.aktivitetskort

		hendelseService.deltakerHendelse(ctx.deltaker.id, ctx.deltaker.toDto(), offset)

		verify(exactly = 1) { deltakerRepository.upsert(ctx.deltaker, offset) }
		verify(exactly = 1) { aktivitetskortService.lagAktivitetskort(ctx.deltaker) }
	}

	@Test
	fun `deltakerHendelse - deltaker lagd - publiser melding`() {
		val ctx = TestData.MockContext()

		every { deltakerRepository.upsert(ctx.deltaker, offset) } returns RepositoryResult.Created(ctx.deltaker)
		every { aktivitetskortService.lagAktivitetskort(ctx.deltaker) } returns ctx.aktivitetskort

		hendelseService.deltakerHendelse(ctx.deltaker.id, ctx.deltaker.toDto(), offset)

		verify(exactly = 1) { deltakerRepository.upsert(ctx.deltaker, offset) }
		verify(exactly = 1) { aktivitetskortService.lagAktivitetskort(ctx.deltaker) }
	}

	@Test
	fun `deltakerHendelse - deltaker har ingen forandring - ikke publiser melding`() {
		val ctx = TestData.MockContext()

		every { deltakerRepository.upsert(ctx.deltaker, offset) } returns RepositoryResult.NoChange()

		hendelseService.deltakerHendelse(ctx.deltaker.id, ctx.deltaker.toDto(), offset)

		verify(exactly = 1) { deltakerRepository.upsert(ctx.deltaker, offset) }
		verify(exactly = 0) { aktivitetskortService.lagAktivitetskort(ctx.deltaker) }
	}

	@Test
	fun `deltakerHendelse - deltaker finnes ikke, status feilregistrert - publiserer ikke melding`() {
		val ctx = TestData.MockContext()
		val mockDeltaker = ctx.deltaker.copy(status = DeltakerStatus(DeltakerStatus.Type.FEILREGISTRERT, null))

		every { deltakerRepository.upsert(mockDeltaker, offset) } returns RepositoryResult.NoChange()

		hendelseService.deltakerHendelse(mockDeltaker.id, mockDeltaker.toDto(), offset)

		verify(exactly = 1) { deltakerRepository.upsert(mockDeltaker, offset) }
		verify(exactly = 0) { aktivitetskortService.lagAktivitetskort(mockDeltaker) }
	}

	@Test
	fun `deltakerHendelse - deltaker finnes, status feilregistrert - publiserer melding`() {
		val ctx = TestData.MockContext()
		val mockDeltaker = ctx.deltaker.copy(status = DeltakerStatus(DeltakerStatus.Type.FEILREGISTRERT, null))

		every { deltakerRepository.upsert(mockDeltaker, offset) } returns RepositoryResult.Modified(mockDeltaker)
		every { aktivitetskortService.lagAktivitetskort(mockDeltaker) } returns ctx.aktivitetskort.copy(aktivitetStatus = AktivitetStatus.AVBRUTT)

		hendelseService.deltakerHendelse(mockDeltaker.id, mockDeltaker.toDto(), offset)

		verify(exactly = 1) { deltakerRepository.upsert(mockDeltaker, offset) }
		verify(exactly = 1) { aktivitetskortService.lagAktivitetskort(mockDeltaker) }
	}

	@Test
	fun `deltakerlisteHendelse - deltakerliste modifisert - publiser melding`() {
		val ctx = TestData.MockContext()

		every { arrangorRepository.get(ctx.arrangor.organisasjonsnummer) } returns ctx.arrangor
		every { deltakerlisteRepository.upsert(ctx.deltakerliste) } returns RepositoryResult.Modified(ctx.deltakerliste)
		every { aktivitetskortService.lagAktivitetskort(ctx.deltakerliste) } returns listOf(ctx.aktivitetskort)

		hendelseService.deltakerlisteHendelse(ctx.deltakerliste.id, ctx.deltakerlisteDto())

		verify(exactly = 1) { deltakerlisteRepository.upsert(ctx.deltakerliste) }
		verify(exactly = 1) { aktivitetskortService.lagAktivitetskort(ctx.deltakerliste) }
	}

	@Test
	fun `deltakerlisteHendelse - deltakerliste lagd - ikke publiser melding`() {
		val ctx = TestData.MockContext()

		every { arrangorRepository.get(ctx.arrangor.organisasjonsnummer) } returns ctx.arrangor
		every { deltakerlisteRepository.upsert(ctx.deltakerliste) } returns RepositoryResult.Created(ctx.deltakerliste)

		hendelseService.deltakerlisteHendelse(ctx.deltakerliste.id, ctx.deltakerlisteDto())

		verify(exactly = 1) { deltakerlisteRepository.upsert(ctx.deltakerliste) }
		verify(exactly = 0) { aktivitetskortService.lagAktivitetskort(ctx.deltakerliste) }
	}

	@Test
	fun `deltakerlisteHendelse - deltakerliste har ingen forandring - ikke publiser melding`() {
		val ctx = TestData.MockContext()

		every { arrangorRepository.get(ctx.arrangor.organisasjonsnummer) } returns ctx.arrangor
		every { deltakerlisteRepository.upsert(ctx.deltakerliste) } returns RepositoryResult.NoChange()

		hendelseService.deltakerlisteHendelse(ctx.deltakerliste.id, ctx.deltakerlisteDto())

		verify(exactly = 1) { deltakerlisteRepository.upsert(ctx.deltakerliste) }
		verify(exactly = 0) { aktivitetskortService.lagAktivitetskort(ctx.deltakerliste) }
	}

	@Test
	fun `deltakerlisteHendelse - tiltak er ikke støttet - skal ikke lagre deltakerliste`() {
		val arrangor = TestData.arrangor()
		val deltakerlisteDto = DeltakerlisteDto(
			id = UUID.randomUUID(),
			navn = "navn",
			tiltakstype = DeltakerlisteDto.Tiltakstype("Ukjent", "UKJENT"),
			virksomhetsnummer = arrangor.organisasjonsnummer,
		)

		hendelseService.deltakerlisteHendelse(deltakerlisteDto.id, deltakerlisteDto)

		verify(exactly = 0) { arrangorRepository.get(arrangor.organisasjonsnummer) }
		verify(exactly = 0) { deltakerlisteRepository.upsert(any()) }
	}

	@Test
	fun `deltakerlisteHendelse - arrangor er ikke lagret - skal hente arrangor fra amt-arrangor`() {
		val arrangor = TestData.arrangor()
		val deltakerliste =
			TestData.deltakerliste(tiltak = Tiltak("navn", Tiltak.Type.INDOPPFAG), arrangorId = arrangor.id)

		every { arrangorRepository.get(arrangor.organisasjonsnummer) } returns null andThen arrangor
		every { arrangorRepository.upsert(any()) } returns RepositoryResult.Created(arrangor)
		every { amtArrangorClient.hentArrangor(arrangor.organisasjonsnummer) } returns AmtArrangorClient.ArrangorMedOverordnetArrangorDto(arrangor.id, arrangor.navn, arrangor.organisasjonsnummer, null)
		every { deltakerlisteRepository.upsert(deltakerliste) } returns RepositoryResult.Created(deltakerliste)

		hendelseService.deltakerlisteHendelse(deltakerliste.id, deltakerliste.toDto(arrangor))

		verify(exactly = 2) { arrangorRepository.get(arrangor.organisasjonsnummer) }
		verify(exactly = 1) { amtArrangorClient.hentArrangor(arrangor.organisasjonsnummer) }
		verify(exactly = 1) { deltakerlisteRepository.upsert(deltakerliste) }
	}

	@Test
	fun `arrangorHendelse - arrangor modifisert - publiser melding`() {
		val ctx = TestData.MockContext()

		every { arrangorRepository.upsert(ctx.arrangor) } returns RepositoryResult.Modified(ctx.arrangor)
		every { aktivitetskortService.lagAktivitetskort(ctx.arrangor) } returns listOf(ctx.aktivitetskort)

		hendelseService.arrangorHendelse(ctx.arrangor.id, ctx.arrangor.toDto())

		verify(exactly = 1) { arrangorRepository.upsert(ctx.arrangor) }
		verify(exactly = 1) { aktivitetskortService.lagAktivitetskort(ctx.arrangor) }
	}

	@Test
	fun `arrangorHendelse - arrangor lagd - ikke publiser melding`() {
		val ctx = TestData.MockContext()

		every { arrangorRepository.upsert(ctx.arrangor) } returns RepositoryResult.Created(ctx.arrangor)

		hendelseService.arrangorHendelse(ctx.arrangor.id, ctx.arrangor.toDto())

		verify(exactly = 1) { arrangorRepository.upsert(ctx.arrangor) }
		verify(exactly = 0) { aktivitetskortService.lagAktivitetskort(ctx.arrangor) }
	}

	@Test
	fun `arrangorHendelse - arrangor har ingen forandring - ikke publiser melding`() {
		val ctx = TestData.MockContext()

		every { arrangorRepository.upsert(ctx.arrangor) } returns RepositoryResult.NoChange()

		hendelseService.arrangorHendelse(ctx.arrangor.id, ctx.arrangor.toDto())

		verify(exactly = 1) { arrangorRepository.upsert(ctx.arrangor) }
		verify(exactly = 0) { aktivitetskortService.lagAktivitetskort(ctx.arrangor) }
	}
}
