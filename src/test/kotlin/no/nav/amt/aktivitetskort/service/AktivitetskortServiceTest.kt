package no.nav.amt.aktivitetskort.service

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.aktivitetskort.repositories.ArrangorRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerlisteRepository
import no.nav.amt.aktivitetskort.repositories.MeldingRepository
import no.nav.amt.aktivitetskort.utils.shouldBeCloseTo
import org.junit.jupiter.api.Test

class AktivitetskortServiceTest {
	private val meldingRepository = mockk<MeldingRepository>(relaxUnitFun = true)
	private val arrangorRepository = mockk<ArrangorRepository>()
	private val deltakerlisteRepository = mockk<DeltakerlisteRepository>()
	private val deltakerRepository = mockk<DeltakerRepository>()

	private val aktivitetskortService = AktivitetskortService(
		meldingRepository = meldingRepository,
		arrangorRepository = arrangorRepository,
		deltakerlisteRepository = deltakerlisteRepository,
		deltakerRepository = deltakerRepository,
	)

	@Test
	fun `lagAktivitetskort(deltaker) - meldinger finnes ikke - lager nytt aktivitetskort`() {
		val ctx = TestData.MockContext()
		every { meldingRepository.getByDeltakerId(ctx.deltaker.id) } returns null
		every { deltakerlisteRepository.get(ctx.deltakerliste.id) } returns ctx.deltakerliste
		every { arrangorRepository.get(ctx.arrangor.id) } returns ctx.arrangor

		val aktivitetskort = aktivitetskortService.lagAktivitetskort(ctx.deltaker).first()

		verify(exactly = 1) { meldingRepository.upsert(any()) }

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
		aktivitetskort.handlinger shouldBe ctx.aktivitetskort.handlinger
		aktivitetskort.detaljer shouldBe ctx.aktivitetskort.detaljer
		aktivitetskort.etiketter shouldBe ctx.aktivitetskort.etiketter
	}

	@Test
	fun `lagAktivitetskort(deltaker) - meldinger finnes - lager nytt aktivitetskort, beholder id`() {
		val ctx = TestData.MockContext()
		every { meldingRepository.getByDeltakerId(ctx.deltaker.id) } returns ctx.melding
		every { deltakerlisteRepository.get(ctx.deltakerliste.id) } returns ctx.deltakerliste
		every { arrangorRepository.get(ctx.arrangor.id) } returns ctx.arrangor

		val aktivitetskort = aktivitetskortService.lagAktivitetskort(ctx.deltaker).first()

		verify(exactly = 1) { meldingRepository.upsert(any()) }

		aktivitetskort shouldBe ctx.aktivitetskort
	}

	@Test
	fun `lagAktivitetskort(deltakerlist) - meldinger finnes - lager nye aktivitetskort`() {
		val ctx = TestData.MockContext()

		every { meldingRepository.getByDeltakerlisteId(ctx.deltakerliste.id) } returns listOf(ctx.melding)
		every { deltakerRepository.get(ctx.deltaker.id) } returns ctx.deltaker
		every { deltakerlisteRepository.get(ctx.deltakerliste.id) } returns ctx.deltakerliste
		every { arrangorRepository.get(ctx.arrangor.id) } returns ctx.arrangor

		val aktivitetskort = aktivitetskortService.lagAktivitetskort(ctx.deltakerliste)

		verify(exactly = 1) { meldingRepository.upsert(any()) }

		aktivitetskort shouldHaveSize 1

		aktivitetskort.first() shouldBe ctx.aktivitetskort
	}

	@Test
	fun `lagAktivitetskort(arrangor) - meldinger finnes - lager nye aktivitetskort`() {
		val ctx = TestData.MockContext()

		every { meldingRepository.getByArrangorId(ctx.arrangor.id) } returns listOf(ctx.melding)
		every { deltakerRepository.get(ctx.deltaker.id) } returns ctx.deltaker
		every { deltakerlisteRepository.get(ctx.deltakerliste.id) } returns ctx.deltakerliste
		every { arrangorRepository.get(ctx.arrangor.id) } returns ctx.arrangor

		val aktivitetskort = aktivitetskortService.lagAktivitetskort(ctx.arrangor)

		verify(exactly = 1) { meldingRepository.upsert(any()) }

		aktivitetskort shouldHaveSize 1

		aktivitetskort.first() shouldBe ctx.aktivitetskort
	}
}
