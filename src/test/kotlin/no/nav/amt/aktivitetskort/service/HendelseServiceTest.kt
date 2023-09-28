package no.nav.amt.aktivitetskort.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.aktivitetskort.client.AmtArrangorClient
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.aktivitetskort.database.TestData.toDto
import no.nav.amt.aktivitetskort.domain.AktivitetStatus
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.aktivitetskort.repositories.ArrangorRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerlisteRepository
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import org.junit.jupiter.api.Test

class HendelseServiceTest {
	private val arrangorRepository = mockk<ArrangorRepository>()
	private val deltakerlisteRepository = mockk<DeltakerlisteRepository>()
	private val deltakerRepository = mockk<DeltakerRepository>()
	private val aktivitetskortService = mockk<AktivitetskortService>()
	private val amtArrangorClient = mockk<AmtArrangorClient>()

	private val hendelseService = HendelseService(
		arrangorRepository = arrangorRepository,
		deltakerlisteRepository = deltakerlisteRepository,
		deltakerRepository = deltakerRepository,
		aktivitetskortService = aktivitetskortService,
		amtArrangorClient = amtArrangorClient,
	)

	@Test
	fun `deltakerHendelse - deltaker modifisert - publiser melding`() {
		val ctx = TestData.MockContext()

		every { deltakerRepository.upsert(ctx.deltaker) } returns RepositoryResult.Modified(ctx.deltaker)
		every { aktivitetskortService.lagAktivitetskort(ctx.deltaker) } returns ctx.aktivitetskort

		hendelseService.deltakerHendelse(ctx.deltaker.id, ctx.deltaker.toDto())

		verify(exactly = 1) { deltakerRepository.upsert(ctx.deltaker) }
		verify(exactly = 1) { aktivitetskortService.lagAktivitetskort(ctx.deltaker) }
	}

	@Test
	fun `deltakerHendelse - deltaker lagd - publiser melding`() {
		val ctx = TestData.MockContext()

		every { deltakerRepository.upsert(ctx.deltaker) } returns RepositoryResult.Created(ctx.deltaker)
		every { aktivitetskortService.lagAktivitetskort(ctx.deltaker) } returns ctx.aktivitetskort

		hendelseService.deltakerHendelse(ctx.deltaker.id, ctx.deltaker.toDto())

		verify(exactly = 1) { deltakerRepository.upsert(ctx.deltaker) }
		verify(exactly = 1) { aktivitetskortService.lagAktivitetskort(ctx.deltaker) }
	}

	@Test
	fun `deltakerHendelse - deltaker har ingen forandring - ikke publiser melding`() {
		val ctx = TestData.MockContext()

		every { deltakerRepository.upsert(ctx.deltaker) } returns RepositoryResult.NoChange()

		hendelseService.deltakerHendelse(ctx.deltaker.id, ctx.deltaker.toDto())

		verify(exactly = 1) { deltakerRepository.upsert(ctx.deltaker) }
		verify(exactly = 0) { aktivitetskortService.lagAktivitetskort(ctx.deltaker) }
	}

	@Test
	fun `deltakerHendelse - deltaker finnes ikke, status feilregistrert - publiserer ikke melding`() {
		val ctx = TestData.MockContext()
		val mockDeltaker = ctx.deltaker.copy(status = DeltakerStatus(DeltakerStatus.Type.FEILREGISTRERT, null))

		every { deltakerRepository.upsert(mockDeltaker) } returns RepositoryResult.NoChange()

		hendelseService.deltakerHendelse(mockDeltaker.id, mockDeltaker.toDto())

		verify(exactly = 1) { deltakerRepository.upsert(mockDeltaker) }
		verify(exactly = 0) { aktivitetskortService.lagAktivitetskort(mockDeltaker) }
	}

	@Test
	fun `deltakerHendelse - deltaker finnes, status feilregistrert - publiserer melding`() {
		val ctx = TestData.MockContext()
		val mockDeltaker = ctx.deltaker.copy(status = DeltakerStatus(DeltakerStatus.Type.FEILREGISTRERT, null))

		every { deltakerRepository.upsert(mockDeltaker) } returns RepositoryResult.Modified(mockDeltaker)
		every { aktivitetskortService.lagAktivitetskort(mockDeltaker) } returns ctx.aktivitetskort.copy(aktivitetStatus = AktivitetStatus.AVBRUTT)

		hendelseService.deltakerHendelse(mockDeltaker.id, mockDeltaker.toDto())

		verify(exactly = 1) { deltakerRepository.upsert(mockDeltaker) }
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
		val deltakerliste =
			TestData.deltakerliste(tiltak = Tiltak("navn", Tiltak.Type.UKJENT), arrangorId = arrangor.id)

		hendelseService.deltakerlisteHendelse(deltakerliste.id, deltakerliste.toDto(arrangor))

		verify(exactly = 0) { arrangorRepository.get(arrangor.organisasjonsnummer) }
		verify(exactly = 0) { deltakerlisteRepository.upsert(deltakerliste) }
	}

	@Test
	fun `deltakerlisteHendelse - arrangor er ikke lagret - skal hente arrangor fra amt-arrangor`() {
		val arrangor = TestData.arrangor()
		val deltakerliste =
			TestData.deltakerliste(tiltak = Tiltak("navn", Tiltak.Type.OPPFOELGING), arrangorId = arrangor.id)

		every { arrangorRepository.get(arrangor.organisasjonsnummer) } returns null
		every { amtArrangorClient.hentArrangor(arrangor.organisasjonsnummer) } returns arrangor
		every { deltakerlisteRepository.upsert(deltakerliste) } returns RepositoryResult.Created(deltakerliste)

		hendelseService.deltakerlisteHendelse(deltakerliste.id, deltakerliste.toDto(arrangor))

		verify(exactly = 1) { arrangorRepository.get(arrangor.organisasjonsnummer) }
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
