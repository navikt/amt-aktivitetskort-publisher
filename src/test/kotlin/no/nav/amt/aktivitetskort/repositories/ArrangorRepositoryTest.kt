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

class ArrangorRepositoryTest : IntegrationTest() {

	@Autowired
	private lateinit var db: TestDatabaseService

	@AfterEach
	fun tearDown() {
		db.clean()
	}

	@Test
	fun `get - finnes ikke - returnerer null`() {
		db.arrangorRepository.get(UUID.randomUUID()) shouldBe null
	}

	@Test
	fun `upsert - finnes ikke - returnerer Created Result - finnes in database`() {
		val arrangor = db.arrangor()
		when (val result = db.arrangorRepository.upsert(arrangor)) {
			is RepositoryResult.Created -> result.data shouldBe arrangor
			else -> fail("Should be Created, was $result")
		}

		db.arrangorRepository.get(arrangor.id) shouldBe arrangor
	}

	@Test
	fun `upsert - finnes - returnerer NoChange Result`() {
		val arrangor = db.arrangor()
			.also { db.arrangorRepository.upsert(it) }

		when (val result = db.arrangorRepository.upsert(arrangor)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}
	}

	@Test
	fun `upsert - endret - returnerer Modified Result og oppdaterer database`() {
		val initialArrangor = db.arrangor()
			.also { db.arrangorRepository.upsert(it) }

		val updatedArrangor = initialArrangor.copy(
			navn = "UPDATED",
		)

		when (val result = db.arrangorRepository.upsert(updatedArrangor)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedArrangor
			else -> fail("Should be Modified, was $result")
		}

		db.arrangorRepository.get(initialArrangor.id) shouldBe updatedArrangor
	}
}
