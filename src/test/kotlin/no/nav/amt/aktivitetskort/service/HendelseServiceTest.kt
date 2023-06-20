package no.nav.amt.aktivitetskort.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.aktivitetskort.database.TestData.toDto
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

	private val hendelseService = HendelseService(
		arrangorRepository = arrangorRepository,
		deltakerlisteRepository = deltakerlisteRepository,
		deltakerRepository = deltakerRepository,
		aktivitetskortService = aktivitetskortService,
	)

	@Test
	fun `deltakerHendelse - deltaker modifisert - publiser melding`() {
		val ctx = TestData.MockContext()

		every { deltakerRepository.upsert(ctx.deltaker) } returns RepositoryResult.Modified(ctx.deltaker)
		every { aktivitetskortService.lagAktivitetskort(ctx.deltaker) } returns listOf(ctx.aktivitetskort)

		hendelseService.deltakerHendelse(ctx.deltaker.id, ctx.deltaker.toDto())

		verify(exactly = 1) { hendelseService.publiserMeldinger(ctx.deltaker) }
	}

	@Test
	fun `deltakerHendelse - deltaker lagd - publiser melding`() {
		val ctx = TestData.MockContext()

		every { deltakerRepository.upsert(ctx.deltaker) } returns RepositoryResult.Created(ctx.deltaker)
		every { aktivitetskortService.lagAktivitetskort(ctx.deltaker) } returns listOf(ctx.aktivitetskort)

		hendelseService.deltakerHendelse(ctx.deltaker.id, ctx.deltaker.toDto())

		verify(exactly = 1) { hendelseService.publiserMeldinger(ctx.deltaker) }
	}

	@Test
	fun `deltakerHendelse - deltaker har ingen forandring - ikke publiser melding`() {
		val ctx = TestData.MockContext()

		every { deltakerRepository.upsert(ctx.deltaker) } returns RepositoryResult.NoChange()

		hendelseService.deltakerHendelse(ctx.deltaker.id, ctx.deltaker.toDto())

		verify(exactly = 0) { hendelseService.publiserMeldinger(ctx.deltaker) }
	}

	@Test
	fun `deltakerlisteHendelse - deltakerliste modifisert - publiser melding`() {
		val ctx = TestData.MockContext()

		every { deltakerlisteRepository.upsert(ctx.deltakerliste) } returns RepositoryResult.Modified(ctx.deltakerliste)
		every { aktivitetskortService.lagAktivitetskort(ctx.deltakerliste) } returns listOf(ctx.aktivitetskort)

		hendelseService.deltakerlisteHendelse(ctx.deltakerliste.id, ctx.deltakerliste.toDto())

		verify(exactly = 1) { hendelseService.publiserMeldinger(ctx.deltakerliste) }
	}

	@Test
	fun `deltakerlisteHendelse - deltakerliste lagd - ikke publiser melding`() {
		val ctx = TestData.MockContext()

		every { deltakerlisteRepository.upsert(ctx.deltakerliste) } returns RepositoryResult.Created(ctx.deltakerliste)

		hendelseService.deltakerlisteHendelse(ctx.deltakerliste.id, ctx.deltakerliste.toDto())

		verify(exactly = 0) { hendelseService.publiserMeldinger(ctx.deltakerliste) }
	}

	@Test
	fun `deltakerlisteHendelse - deltakerliste har ingen forandring - ikke publiser melding`() {
		val ctx = TestData.MockContext()

		every { deltakerlisteRepository.upsert(ctx.deltakerliste) } returns RepositoryResult.NoChange()

		hendelseService.deltakerlisteHendelse(ctx.deltakerliste.id, ctx.deltakerliste.toDto())

		verify(exactly = 0) { hendelseService.publiserMeldinger(ctx.deltakerliste) }
	}

	@Test
	fun `arrangorHendelse - arrangor modifisert - publiser melding`() {
		val ctx = TestData.MockContext()

		every { arrangorRepository.upsert(ctx.arrangor) } returns RepositoryResult.Modified(ctx.arrangor)
		every { aktivitetskortService.lagAktivitetskort(ctx.arrangor) } returns listOf(ctx.aktivitetskort)

		hendelseService.arrangorHendelse(ctx.arrangor.id, ctx.arrangor.toDto())

		verify(exactly = 1) { hendelseService.publiserMeldinger(ctx.arrangor) }
	}

	@Test
	fun `arrangorHendelse - arrangor lagd - ikke publiser melding`() {
		val ctx = TestData.MockContext()

		every { arrangorRepository.upsert(ctx.arrangor) } returns RepositoryResult.Created(ctx.arrangor)

		hendelseService.arrangorHendelse(ctx.arrangor.id, ctx.arrangor.toDto())

		verify(exactly = 0) { hendelseService.publiserMeldinger(ctx.arrangor) }
	}

	@Test
	fun `arrangorHendelse - arrangor har ingen forandring - ikke publiser melding`() {
		val ctx = TestData.MockContext()

		every { arrangorRepository.upsert(ctx.arrangor) } returns RepositoryResult.NoChange()

		hendelseService.arrangorHendelse(ctx.arrangor.id, ctx.arrangor.toDto())

		verify(exactly = 0) { hendelseService.publiserMeldinger(ctx.arrangor) }
	}
}
