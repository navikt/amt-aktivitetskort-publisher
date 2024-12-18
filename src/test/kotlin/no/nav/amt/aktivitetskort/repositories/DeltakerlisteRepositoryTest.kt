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

class DeltakerlisteRepositoryTest : IntegrationTest() {
	@Autowired
	private lateinit var db: TestDatabaseService

	@Test
	fun `get - finnes ikke - returnerer null`() {
		db.deltakerlisteRepository.get(UUID.randomUUID()) shouldBe null
	}

	@Test
	fun `upsert - finnes ikke - returnerer Created Result - finnes in database`() {
		val deltakerliste = TestData.deltakerliste()
		db.insertArrangor(TestData.arrangor(deltakerliste.arrangorId))
		when (val result = db.deltakerlisteRepository.upsert(deltakerliste)) {
			is RepositoryResult.Created -> result.data shouldBe deltakerliste
			else -> fail("Should be Created, was $result")
		}

		db.deltakerlisteRepository.get(deltakerliste.id) shouldBe deltakerliste
	}

	@Test
	fun `upsert - finnes - returnerer NoChange Result`() {
		val deltakerliste = TestData
			.deltakerliste()
			.also { db.insertArrangor(TestData.arrangor(it.arrangorId)) }
			.also { db.deltakerlisteRepository.upsert(it) }

		when (val result = db.deltakerlisteRepository.upsert(deltakerliste)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}
	}

	@Test
	fun `upsert - endret - returnerer Modified Result og oppdaterer database`() {
		val initialDeltakerliste = TestData
			.deltakerliste()
			.also { db.insertArrangor(TestData.arrangor(it.arrangorId)) }
			.also { db.deltakerlisteRepository.upsert(it) }

		val updatedDeltakerliste = initialDeltakerliste.copy(
			navn = "UPDATED",
		)

		when (val result = db.deltakerlisteRepository.upsert(updatedDeltakerliste)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedDeltakerliste
			else -> fail("Should be Modified, was $result")
		}

		db.deltakerlisteRepository.get(initialDeltakerliste.id) shouldBe updatedDeltakerliste
	}

	@Test
	fun `upsert - endret arrangor - returnerer Modified Result og oppdaterer database`() {
		val initialDeltakerliste = TestData
			.deltakerliste()
			.also { db.insertArrangor(TestData.arrangor(it.arrangorId)) }
			.also { db.deltakerlisteRepository.upsert(it) }
		val nyArrangor = TestData
			.arrangor()
			.also { db.insertArrangor(it) }

		val updatedDeltakerliste = initialDeltakerliste.copy(
			arrangorId = nyArrangor.id,
		)

		when (val result = db.deltakerlisteRepository.upsert(updatedDeltakerliste)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedDeltakerliste
			else -> fail("Should be Modified, was $result")
		}

		db.deltakerlisteRepository.get(initialDeltakerliste.id) shouldBe updatedDeltakerliste
	}
}
