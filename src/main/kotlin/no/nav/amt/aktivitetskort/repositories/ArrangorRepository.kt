package no.nav.amt.aktivitetskort.repositories

import no.nav.amt.aktivitetskort.domain.Arrangor
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import no.nav.amt.aktivitetskort.utils.sqlParameters
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ArrangorRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		Arrangor(
			id = UUID.fromString(rs.getString("id")),
			navn = rs.getString("navn")
		)
	}

	fun insertOrUpdate(arrangor: Arrangor): RepositoryResult<Arrangor> {
		val old = get(arrangor.id)

		if (old == arrangor) return RepositoryResult.NoChange()

		val new = template.query(
			"""
			INSERT INTO arrangor(id, navn)
			VALUES (:id,
					:navn)
			ON CONFLICT (id) DO UPDATE SET navn        = :navn
			RETURNING *
			""".trimIndent(),
			sqlParameters(
				"id" to arrangor.id,
				"navn" to arrangor.navn
			),
			rowMapper
		).first()

		if (old == null) return RepositoryResult.Created(new)

		return RepositoryResult.Modified(new)
	}

	fun get(id: UUID): Arrangor? = template.query(
		"SELECT * from arrangor where id = :id",
		sqlParameters("id" to id),
		rowMapper
	).firstOrNull()
}