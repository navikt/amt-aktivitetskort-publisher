package no.nav.amt.aktivitetskort.repositories

import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import org.junit.jupiter.api.Test
import java.util.UUID

class ArrangorRepositoryTest : IntegrationTest() {
	@Test
	fun `get(uuid) - finnes - returnerer arrangor`() {
		val arrangor = TestData.arrangor()
		testDatabase.insertArrangor(arrangor)
		testDatabase.arrangorRepository.get(arrangor.id) shouldBe arrangor
	}

	@Test
	fun `get(uuid) - finnes ikke - returnerer null`() {
		testDatabase.arrangorRepository.get(UUID.randomUUID()) shouldBe null
	}

	@Test
	fun `get(orgnr) - finnes - returnerer arrangor`() {
		val arrangor = TestData.arrangor()
		testDatabase.insertArrangor(arrangor)
		testDatabase.arrangorRepository.get(arrangor.organisasjonsnummer) shouldBe arrangor
	}

	@Test
	fun `get(orgnr) - finnes ikke - returnerer null`() {
		testDatabase.arrangorRepository.get("foobar") shouldBe null
	}

	@Test
	fun `upsert - finnes ikke - returnerer Created Result - finnes in database`() {
		val arrangor = TestData.arrangor()
		when (val result = testDatabase.arrangorRepository.upsert(arrangor)) {
			is RepositoryResult.Created -> result.data shouldBe arrangor
			else -> fail("Should be Created, was $result")
		}

		testDatabase.arrangorRepository.get(arrangor.id) shouldBe arrangor
	}

	@Test
	fun `upsert - finnes - returnerer NoChange Result`() {
		val arrangor = TestData
			.arrangor()
			.also { testDatabase.arrangorRepository.upsert(it) }

		when (val result = testDatabase.arrangorRepository.upsert(arrangor)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}
	}

	@Test
	fun `upsert - endret - returnerer Modified Result og oppdaterer database`() {
		val initialArrangor = TestData
			.arrangor()
			.also { testDatabase.arrangorRepository.upsert(it) }

		val updatedArrangor = initialArrangor.copy(
			navn = "UPDATED",
		)

		when (val result = testDatabase.arrangorRepository.upsert(updatedArrangor)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedArrangor
			else -> fail("Should be Modified, was $result")
		}

		testDatabase.arrangorRepository.get(initialArrangor.id) shouldBe updatedArrangor
	}
}
