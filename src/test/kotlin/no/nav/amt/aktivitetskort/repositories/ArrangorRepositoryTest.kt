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
	fun `get - not exists - returns null`() {
		db.arrangorRepository.get(UUID.randomUUID()) shouldBe null
	}

	@Test
	fun `insertOrUpdate - not exists - returns Created Result - exists in database`() {
		val arrangor = db.arrangor()
		when (val result = db.arrangorRepository.insertOrUpdate(arrangor)) {
			is RepositoryResult.Created -> result.data shouldBe arrangor
			else -> fail("Should be Created, was $result")
		}

		db.arrangorRepository.get(arrangor.id) shouldBe arrangor
	}

	@Test
	fun `insertOrUpdate - exists - returns NoChange Result`() {
		val arrangor = db.arrangor()
			.also { db.arrangorRepository.insertOrUpdate(it) }

		when (val result = db.arrangorRepository.insertOrUpdate(arrangor)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}
	}

	@Test
	fun `insertOrUpdate - modified - returns Modified Result and updates database`() {
		val initialArrangor = db.arrangor()
			.also { db.arrangorRepository.insertOrUpdate(it) }

		val updatedArrangor = initialArrangor.copy(
			navn = "UPDATED"
		)

		when (val result = db.arrangorRepository.insertOrUpdate(updatedArrangor)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedArrangor
			else -> fail("Should be Modified, was $result")
		}

		db.arrangorRepository.get(initialArrangor.id) shouldBe updatedArrangor
	}
}
