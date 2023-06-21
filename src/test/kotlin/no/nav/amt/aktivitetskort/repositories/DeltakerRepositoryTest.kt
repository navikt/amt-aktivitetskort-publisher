package no.nav.amt.aktivitetskort.repositories

import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.aktivitetskort.database.TestDatabaseService
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class DeltakerRepositoryTest : IntegrationTest() {

	@Autowired
	private lateinit var db: TestDatabaseService

	@Test
	fun `get - finnes ikke - returnerer null`() {
		db.deltakerRepository.get(UUID.randomUUID()) shouldBe null
	}

	@Test
	fun `upsert - finnes ikke - returnerer Created Result - finnes in database`() {
		val deltaker = TestData.deltaker()
			.also { db.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
		when (val result = db.deltakerRepository.upsert(deltaker)) {
			is RepositoryResult.Created -> result.data shouldBe deltaker
			else -> fail("Should be Created, was $result")
		}

		db.deltakerRepository.get(deltaker.id) shouldBe deltaker
	}

	@Test
	fun `upsert - finnes - returnerer NoChange Result`() {
		val deltaker = TestData.deltaker()
			.also { db.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
			.also { db.deltakerRepository.upsert(it) }

		when (val result = db.deltakerRepository.upsert(deltaker)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}
	}

	@Test
	fun `upsert - endret - returnerer Modified Result og oppdaterer database`() {
		val initialDeltaker = TestData.deltaker()
			.also { db.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
			.also { db.deltakerRepository.upsert(it) }

		val updatedDeltaker = initialDeltaker.copy(
			personident = "UPDATED",
		)

		when (val result = db.deltakerRepository.upsert(updatedDeltaker)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedDeltaker
			else -> fail("Should be Modified, was $result")
		}

		db.deltakerRepository.get(initialDeltaker.id) shouldBe updatedDeltaker
	}
}
