package no.nav.amt.aktivitetskort.repositories

import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.database.TestDatabaseService
import no.nav.amt.aktivitetskort.utils.RepositoryResult
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
	fun `upsert - finnes ikke - returnerer Created Result - finnes i database`() {
		val melding = db.melding()
		when (val result = db.meldingRepository.upsert(melding)) {
			is RepositoryResult.Created -> result.data shouldBe melding
			else -> fail("Should be Created, was $result")
		}

		db.meldingRepository.getByDeltakerId(melding.deltakerId) shouldBe melding
	}

	@Test
	fun `upsert - finnes - returnerer NoChange Result`() {
		val melding = db.melding()
			.also { db.meldingRepository.upsert(it) }

		when (val result = db.meldingRepository.upsert(melding)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}
	}

	@Test
	fun `upsert - endret - returnerer Modified Result og oppdaterer database`() {
		val initialMelding = db.melding()
			.also { db.meldingRepository.upsert(it) }

		val updatedMelding = initialMelding.copy(
			aktivitetskort = initialMelding.aktivitetskort.copy(sluttDato = LocalDate.now())
		)

		when (val result = db.meldingRepository.upsert(updatedMelding)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedMelding
			else -> fail("Should be Modified, was $result")
		}

		db.meldingRepository.getByDeltakerId(initialMelding.deltakerId) shouldBe updatedMelding
	}
}
