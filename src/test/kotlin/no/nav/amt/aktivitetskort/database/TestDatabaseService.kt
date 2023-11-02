package no.nav.amt.aktivitetskort.database

import no.nav.amt.aktivitetskort.database.TestData.arrangor
import no.nav.amt.aktivitetskort.database.TestData.deltaker
import no.nav.amt.aktivitetskort.database.TestData.deltakerliste
import no.nav.amt.aktivitetskort.domain.Arrangor
import no.nav.amt.aktivitetskort.domain.Deltaker
import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.repositories.ArrangorRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerlisteRepository
import no.nav.amt.aktivitetskort.repositories.MeldingRepository
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import no.nav.amt.aktivitetskort.utils.sqlParameters
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import javax.sql.DataSource

@Service
class TestDatabaseService(
	val meldingRepository: MeldingRepository,
	val arrangorRepository: ArrangorRepository,
	val deltakerlisteRepository: DeltakerlisteRepository,
	val deltakerRepository: DeltakerRepository,
	private val datasource: DataSource,
	private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
) {

	fun clean() = DbTestDataUtils.cleanDatabase(datasource)

	fun insertDeltaker(deltaker: Deltaker = deltaker(), offset: Long = 0): Deltaker {
		insertDeltakerliste(deltakerliste(id = deltaker.deltakerlisteId))
		return when (val result = deltakerRepository.upsert(deltaker, offset)) {
			is RepositoryResult.Created -> result.data
			is RepositoryResult.Modified -> result.data
			is RepositoryResult.NoChange -> deltaker
		}
	}

	fun insertArrangor(arrangor: Arrangor = arrangor()) =
		when (val result = arrangorRepository.upsert(arrangor)) {
			is RepositoryResult.Created -> result.data
			is RepositoryResult.Modified -> result.data
			is RepositoryResult.NoChange -> arrangor
		}

	fun insertDeltakerliste(deltakerliste: Deltakerliste = deltakerliste()): Deltakerliste {
		arrangorRepository.upsert(arrangor(id = deltakerliste.arrangorId))

		return when (val result = deltakerlisteRepository.upsert(deltakerliste)) {
			is RepositoryResult.Created -> result.data
			is RepositoryResult.Modified -> result.data
			is RepositoryResult.NoChange -> deltakerliste
		}
	}

	fun feilmeldingErLagret(key: UUID): Boolean {
		return namedParameterJdbcTemplate.queryForObject(
			"SELECT exists(SELECT 1 from feilmelding where key = :key)",
			sqlParameters("key" to key),
			Boolean::class.java,
		) ?: false
	}
}
