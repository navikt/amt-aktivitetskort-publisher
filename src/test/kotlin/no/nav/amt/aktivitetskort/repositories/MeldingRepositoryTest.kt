package no.nav.amt.aktivitetskort.repositories

import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.aktivitetskort.database.TestDatabaseService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID

class MeldingRepositoryTest : IntegrationTest() {

	@Autowired
	private lateinit var db: TestDatabaseService

	@AfterEach
	fun tearDown() {
		db.clean()
	}

	@Test
	fun `get - finnes ikke - returnerer null`() {
		db.meldingRepository.getByDeltakerId(UUID.randomUUID()) shouldBe null
	}

	@Test
	fun `upsert - finnes ikke - oppretter ny melding`() {
		val ctx = TestData.MockContext()
		val melding = ctx.melding
			.also { db.insertArrangor(ctx.arrangor) }
			.also { db.insertDeltakerliste(ctx.deltakerliste) }
			.also { db.insertDeltaker(ctx.deltaker) }
			.also { db.meldingRepository.upsert(it) }

		db.meldingRepository.getByDeltakerId(melding.deltakerId)!! shouldBe melding
	}

	@Test
	fun `upsert - melding er endret - oppdaterer melding`() {
		val ctx = TestData.MockContext()
		val initialMelding = ctx.melding
			.also { db.insertArrangor(ctx.arrangor) }
			.also { db.insertDeltakerliste(ctx.deltakerliste) }
			.also { db.insertDeltaker(ctx.deltaker) }
			.also { db.meldingRepository.upsert(it) }

		val updatedMelding = initialMelding.copy(
			aktivitetskort = initialMelding.aktivitetskort.copy(sluttDato = LocalDate.now()),
		)

		db.meldingRepository.upsert(updatedMelding)

		val melding = db.meldingRepository.getByDeltakerId(initialMelding.deltakerId)!!

		melding.aktivitetskort shouldBe updatedMelding.aktivitetskort
	}
}
