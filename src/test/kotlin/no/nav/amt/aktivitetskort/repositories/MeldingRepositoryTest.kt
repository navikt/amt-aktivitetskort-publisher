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
	fun `get - not exists - returns null`() {
		db.meldingRepository.getByDeltakerId(UUID.randomUUID()) shouldBe null
	}

	@Test
	fun `insertOrUpdate - not exists - returns Created Result - exists in database`() {
		val melding = db.melding()
		when (val result = db.meldingRepository.insertOrUpdate(melding)) {
			is RepositoryResult.Created -> result.data shouldBe melding
			else -> fail("Should be Created, was $result")
		}

		db.meldingRepository.getByDeltakerId(melding.deltakerId) shouldBe melding
	}

	@Test
	fun `insertOrUpdate - exists - returns NoChange Result`() {
		val melding = db.melding()
			.also { db.meldingRepository.insertOrUpdate(it) }

		when (val result = db.meldingRepository.insertOrUpdate(melding)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}
	}

	@Test
	fun `insertOrUpdate - modified - returns Modified Result and updates database`() {
		val initialMelding = db.melding()
			.also { db.meldingRepository.insertOrUpdate(it) }

		val updatedMelding = initialMelding.copy(
			melding = initialMelding.melding.copy(sluttDato = LocalDate.now())
		)

		when (val result = db.meldingRepository.insertOrUpdate(updatedMelding)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedMelding
			else -> fail("Should be Modified, was $result")
		}

		db.meldingRepository.getByDeltakerId(initialMelding.deltakerId) shouldBe updatedMelding
	}
}
