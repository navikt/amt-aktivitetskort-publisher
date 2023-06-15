package no.nav.amt.aktivitetskort.repositories

import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.database.TestDatabaseService
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class DeltakerlisteRepositoryTest : IntegrationTest() {

	@Autowired
	private lateinit var db: TestDatabaseService

	@AfterEach
	fun tearDown() {
		db.clean()
	}

	@Test
	fun `get - not exists - returns null`() {
		db.deltakerlisteRepository.get(UUID.randomUUID()) shouldBe null
	}

	@Test
	fun `insertOrUpdate - not exists - returns Created Result - exists in database`() {
		val deltakerliste = db.deltakerliste()
		when (val result = db.deltakerlisteRepository.insertOrUpdate(deltakerliste)) {
			is RepositoryResult.Created -> result.data shouldBe deltakerliste
			else -> fail("Should be Created, was $result")
		}

		db.deltakerlisteRepository.get(deltakerliste.id) shouldBe deltakerliste
	}

	@Test
	fun `insertOrUpdate - exists - returns NoChange Result`() {
		val deltakerliste = db.deltakerliste()
			.also { db.deltakerlisteRepository.insertOrUpdate(it) }

		when (val result = db.deltakerlisteRepository.insertOrUpdate(deltakerliste)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}
	}

	@Test
	fun `insertOrUpdate - modified - returns Modified Result and updates database`() {
		val initialDeltakerliste = db.deltakerliste()
			.also { db.deltakerlisteRepository.insertOrUpdate(it) }

		val updatedDeltakerliste = initialDeltakerliste.copy(
			navn = "UPDATED"
		)

		when (val result = db.deltakerlisteRepository.insertOrUpdate(updatedDeltakerliste)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedDeltakerliste
			else -> fail("Should be Modified, was $result")
		}

		db.deltakerlisteRepository.get(initialDeltakerliste.id) shouldBe updatedDeltakerliste
	}
}
