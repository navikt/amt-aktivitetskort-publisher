package no.nav.amt.aktivitetskort.repositories

import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.database.DbTestData.arrangor
import no.nav.amt.aktivitetskort.database.DbTestDataUtils
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import javax.sql.DataSource

class ArrangorRepositoryTest : IntegrationTest() {

	@Autowired
	private lateinit var repository: ArrangorRepository

	@Autowired
	private lateinit var datasource: DataSource

	@AfterEach
	fun tearDown() {
		DbTestDataUtils.cleanDatabase(datasource)
	}

	@Test
	fun `get - not exists - returns null`() {
		repository.get(UUID.randomUUID()) shouldBe null
	}

	@Test
	fun `insertOrUpdate - not exists - returns Created Result - exists in database`() {
		val arrangor = arrangor()
		when (val result = repository.insertOrUpdate(arrangor)) {
			is RepositoryResult.Created -> result.data shouldBe arrangor
			else -> fail("Should be Created, was $result")
		}

		repository.get(arrangor.id) shouldBe arrangor
	}

	@Test
	fun `insertOrUpdate - exists - returns NoChange Result`() {
		val arrangor = arrangor()
			.also { repository.insertOrUpdate(it) }

		when (val result = repository.insertOrUpdate(arrangor)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}
	}

	@Test
	fun `insertOrUpdate - modified - returns Modified Result and updates database`() {
		val initialArrangor = arrangor()
			.also { repository.insertOrUpdate(it) }

		val updatedArrangor = initialArrangor.copy(
			navn = "UPDATED"
		)

		when (val result = repository.insertOrUpdate(updatedArrangor)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedArrangor
			else -> fail("Should be Modified, was $result")
		}

		repository.get(initialArrangor.id) shouldBe updatedArrangor
	}
}
