package no.nav.amt.aktivitetskort.repositories

import io.getunleash.FakeUnleash
import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class DeltakerRepositoryTest(
	private val unleashClient: FakeUnleash,
) : IntegrationTest() {
	@BeforeEach
	fun setup() {
		unleashClient.disableAll()
	}

	@Test
	fun `get - finnes ikke - returnerer null`() {
		testDatabase.deltakerRepository.get(UUID.randomUUID()) shouldBe null
	}

	@Test
	fun `upsert - finnes ikke - returnerer Created Result - finnes i database`() {
		val deltaker = TestData
			.deltaker()
			.also { testDatabase.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
		when (val result = testDatabase.deltakerRepository.upsert(deltaker, 0)) {
			is RepositoryResult.Created -> result.data shouldBe deltaker
			else -> fail("Should be Created, was $result")
		}

		testDatabase.deltakerRepository.get(deltaker.id) shouldBe deltaker
	}

	@Test
	fun `upsert - finnes, uendret - returnerer NoChange Result`() {
		val deltaker = TestData
			.deltaker(dagerPerUke = null, prosentStilling = null)
			.also { testDatabase.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
			.also { testDatabase.deltakerRepository.upsert(it, 0) }

		when (val result = testDatabase.deltakerRepository.upsert(deltaker, 1)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}
	}

	@Test
	fun `upsert - finnes, uendret, skal oppdatere for uendrede deltakere - returnerer Modified Result`() {
		unleashClient.enableAll()
		val deltaker = TestData
			.deltaker(dagerPerUke = null, prosentStilling = null)
			.also { testDatabase.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
			.also { testDatabase.deltakerRepository.upsert(it, 0) }

		when (val result = testDatabase.deltakerRepository.upsert(deltaker, 1)) {
			is RepositoryResult.Created -> fail("Should be Modified, was $result")
			is RepositoryResult.Modified -> {}
			is RepositoryResult.NoChange -> fail("Should be Modified, was $result")
		}
	}

	@Test
	fun `upsert - endret - returnerer Modified Result og oppdaterer database`() {
		val initialDeltaker = TestData
			.deltaker()
			.also { testDatabase.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
			.also { testDatabase.deltakerRepository.upsert(it, 0) }

		val updatedDeltaker = initialDeltaker.copy(
			personident = "UPDATED",
		)

		when (val result = testDatabase.deltakerRepository.upsert(updatedDeltaker, 1)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedDeltaker
			else -> fail("Should be Modified, was $result")
		}

		testDatabase.deltakerRepository.get(initialDeltaker.id) shouldBe updatedDeltaker
	}

	@Test
	fun `upsert - endret, eldre offset - returnerer NoChange Result og oppdaterer ikke database`() {
		val initialDeltaker = TestData
			.deltaker()
			.also { testDatabase.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
			.also { testDatabase.deltakerRepository.upsert(it, 1) }

		val tidligereVersjonAvDeltaker = initialDeltaker.copy(
			personident = "UTDATERT",
		)

		when (val result = testDatabase.deltakerRepository.upsert(tidligereVersjonAvDeltaker, 0)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}

		testDatabase.deltakerRepository.get(initialDeltaker.id) shouldBe initialDeltaker
	}

	@Test
	fun `upsert - uendret, samme offset - returnerer NoChange Result og oppdaterer ikke database`() {
		val initialDeltaker = TestData
			.deltaker()
			.also { testDatabase.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
			.also { testDatabase.deltakerRepository.upsert(it, 1) }

		when (val result = testDatabase.deltakerRepository.upsert(initialDeltaker, 1)) {
			is RepositoryResult.NoChange -> {}
			else -> fail("Should be NoChange, was $result")
		}

		testDatabase.deltakerRepository.get(initialDeltaker.id) shouldBe initialDeltaker
	}

	@Test
	fun `upsert - uendret, nyere offset - returnerer NoChange Result og oppdaterer ikke database`() {
		val initialDeltaker = TestData
			.deltaker()
			.also { testDatabase.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
			.also { testDatabase.deltakerRepository.upsert(it, 1) }

		when (val result = testDatabase.deltakerRepository.upsert(initialDeltaker, 2)) {
			is RepositoryResult.Created -> fail("Should be NoChange, was $result")
			is RepositoryResult.Modified -> fail("Should be NoChange, was $result")
			is RepositoryResult.NoChange -> {}
		}

		testDatabase.deltakerRepository.get(initialDeltaker.id) shouldBe initialDeltaker
	}

	@Test
	fun `upsert - finnes ikke, status feilregistrert - returnerer NoChange Result, lagres ikke`() {
		val deltaker = TestData
			.deltaker()
			.copy(status = DeltakerStatus(DeltakerStatus.Type.FEILREGISTRERT, null))
			.also { testDatabase.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
		when (val result = testDatabase.deltakerRepository.upsert(deltaker, 0)) {
			is RepositoryResult.NoChange -> {}
			else -> fail("Should be NoChange, was $result")
		}

		testDatabase.deltakerRepository.get(deltaker.id) shouldBe null
	}

	@Test
	fun `upsert - finnes, status feilregistrert - returnerer Modified Result og oppdaterer database`() {
		val initialDeltaker = TestData
			.deltaker()
			.also { testDatabase.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
			.also { testDatabase.deltakerRepository.upsert(it, 0) }

		val updatedDeltaker = initialDeltaker.copy(
			status = DeltakerStatus(DeltakerStatus.Type.FEILREGISTRERT, null),
		)

		when (val result = testDatabase.deltakerRepository.upsert(updatedDeltaker, 1)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedDeltaker
			else -> fail("Should be Modified, was $result")
		}

		testDatabase.deltakerRepository.get(initialDeltaker.id) shouldBe updatedDeltaker
	}

	@Test
	fun `upsert - avsluttet i nov 2017, finnes ikke - returnerer NoChange Result - lagres ikke`() {
		val deltaker = TestData
			.deltaker(
				oppstartsdato = LocalDate.of(2017, 10, 1),
				sluttdato = LocalDate.of(2017, 11, 30),
				status = DeltakerStatus(DeltakerStatus.Type.HAR_SLUTTET, DeltakerStatus.Aarsak.FATT_JOBB),
			).also { testDatabase.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
		when (val result = testDatabase.deltakerRepository.upsert(deltaker, 0)) {
			is RepositoryResult.NoChange -> {}
			else -> fail("Should be NoChange, was $result")
		}

		testDatabase.deltakerRepository.get(deltaker.id) shouldBe null
	}

	@Test
	fun `upsert - avsluttet nov 2017, finnes med annen status - returnerer Modified Result og oppdaterer database`() {
		val initialDeltaker = TestData
			.deltaker(
				oppstartsdato = LocalDate.of(2017, 10, 1),
				sluttdato = null,
				status = DeltakerStatus(DeltakerStatus.Type.DELTAR, null),
			).also { testDatabase.insertDeltakerliste(TestData.deltakerliste(id = it.deltakerlisteId)) }
			.also { testDatabase.deltakerRepository.upsert(it, 0) }

		val updatedDeltaker = initialDeltaker.copy(
			oppstartsdato = LocalDate.of(2017, 10, 1),
			sluttdato = LocalDate.of(2017, 11, 30),
			status = DeltakerStatus(DeltakerStatus.Type.FULLFORT, DeltakerStatus.Aarsak.ANNET),
		)

		when (val result = testDatabase.deltakerRepository.upsert(updatedDeltaker, 1)) {
			is RepositoryResult.Modified -> result.data shouldBe updatedDeltaker
			else -> fail("Should be Modified, was $result")
		}

		testDatabase.deltakerRepository.get(initialDeltaker.id) shouldBe updatedDeltaker
	}
}
