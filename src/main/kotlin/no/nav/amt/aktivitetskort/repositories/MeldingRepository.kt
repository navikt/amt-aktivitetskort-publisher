package no.nav.amt.aktivitetskort.repositories

import no.nav.amt.aktivitetskort.domain.Aktivitetskort
import no.nav.amt.aktivitetskort.domain.Melding
import no.nav.amt.aktivitetskort.utils.JsonUtils
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import no.nav.amt.aktivitetskort.utils.getZonedDateTime
import no.nav.amt.aktivitetskort.utils.sqlParameters
import org.postgresql.util.PGobject
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class MeldingRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		Melding(
			deltakerId = UUID.fromString(rs.getString("deltaker_id")),
			deltakerlisteId = UUID.fromString(rs.getString("deltakerliste_id")),
			arrangorId = UUID.fromString(rs.getString("arrangor_id")),
			melding = JsonUtils.fromJson(rs.getString("melding")),
			createdAt = rs.getZonedDateTime("created_at"),
			modifiedAt = rs.getZonedDateTime("modified_at")
		)
	}

	fun insertOrUpdate(melding: Melding): RepositoryResult<Melding> {
		val old = getByDeltakerId(melding.deltakerId)
		if (old == melding) return RepositoryResult.NoChange()

		val new = template.query(
			"""
			INSERT INTO melding(deltaker_id, deltakerliste_id, arrangor_id, melding)
			VALUES (:deltaker_id,
					:deltakerliste_id,
					:arrangor_id,
					:melding)
			ON CONFLICT (deltaker_id) DO UPDATE SET melding          = :melding,
													deltakerliste_id = :deltakerliste_id,
													arrangor_id      = :arrangor_id,
													modified_at      = current_timestamp
			RETURNING *
			""".trimIndent(),
			sqlParameters(
				"deltaker_id" to melding.deltakerId,
				"deltakerliste_id" to melding.deltakerlisteId,
				"arrangor_id" to melding.arrangorId,
				"melding" to melding.melding.toPGObject()
			),
			rowMapper
		).first()

		if (old == null) return RepositoryResult.Created(new)

		return RepositoryResult.Modified(new)
	}

	fun getByDeltakerId(deltakerId: UUID): Melding? = template.query(
		"SELECT * FROM melding where deltaker_id = :deltaker_id",
		sqlParameters("deltaker_id" to deltakerId),
		rowMapper
	).firstOrNull()

	fun getByDeltakerlisteId(deltakerlisteId: UUID): List<Melding> = template.query(
		"SELECT * FROM melding where deltakerliste_id = :deltakerliste_id",
		sqlParameters("deltakerliste_id" to deltakerlisteId),
		rowMapper
	)

	fun getByArrangorId(arrangorId: UUID): List<Melding> = template.query(
		"SELECT * FROM melding where arrangor_id = :arrangor_id",
		sqlParameters("arrangor_id" to arrangorId),
		rowMapper
	)

	fun Aktivitetskort.toPGObject() = PGobject().also {
		it.type = "json"
		it.value = JsonUtils.objectMapper().writeValueAsString(this)
	}
}
