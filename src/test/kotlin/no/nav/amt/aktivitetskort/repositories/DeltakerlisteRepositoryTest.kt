package no.nav.amt.aktivitetskort.repositories

import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.database.DbTestData.deltakerliste
import no.nav.amt.aktivitetskort.database.DbTestDataUtils
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import javax.sql.DataSource

class DeltakerlisteRepositoryTest : IntegrationTest() {

	@Autowired
	private lateinit var repository: DeltakerlisteRepository

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
		val deltakerliste = deltakerliste()
		when (val result = repository.insertOrUpdate(deltakerliste)) {
			is RepositoryResult.Created -> result.data shouldBe deltakerliste
			else -> fail("Should be Created, was $result")
		}

		repository.get(deltakerliste.id) shouldBe deltakerliste
	}

	@Test
	fun `insertOrUpdate - exists - returns NoChange Result`() {
		val deltakerliste = deltakerliste()
			.also { repository.insertOrUpdate(it) }

		when (val result = repository.insertOrUpdate(deltakerliste)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}
	}

	@Test
	fun `insertOrUpdate - modified - returns Modified Result and updates database`() {
		val initialDeltakerliste = deltakerliste()
			.also { repository.insertOrUpdate(it) }

		val updatedDeltakerliste = initialDeltakerliste.copy(
			navn = "UPDATED"
		)

		when (val result = repository.insertOrUpdate(updatedDeltakerliste)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedDeltakerliste
			else -> fail("Should be Modified, was $result")
		}

		repository.get(initialDeltakerliste.id) shouldBe updatedDeltakerliste
	}
}
