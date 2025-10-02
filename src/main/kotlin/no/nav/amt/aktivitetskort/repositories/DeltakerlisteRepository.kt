package no.nav.amt.aktivitetskort.repositories

import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import no.nav.amt.aktivitetskort.utils.sqlParameters
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class DeltakerlisteRepository(
	private val template: NamedParameterJdbcTemplate,
) {
	private val rowMapper = RowMapper { rs, _ ->
		Deltakerliste(
			id = UUID.fromString(rs.getString("id")),
			tiltak = Tiltak(
				navn = rs.getString("tiltaksnavn"),
				tiltakskode = Tiltakskode.valueOf(rs.getString("tiltakstype")),
			),
			navn = rs.getString("navn"),
			arrangorId = UUID.fromString(rs.getString("arrangor_id")),
		)
	}

	fun upsert(deltakerliste: Deltakerliste): RepositoryResult<Deltakerliste> {
		val old = get(deltakerliste.id)

		if (old == deltakerliste) return RepositoryResult.NoChange()

		val new = template
			.query(
				"""
				INSERT INTO deltakerliste(id, tiltaksnavn, tiltakstype, navn, arrangor_id)
				VALUES (:id,
						:tiltaksnavn,
						:tiltakstype,
						:navn,
						:arrangor_id
						)
				ON CONFLICT (id) DO UPDATE SET
					tiltaksnavn = :tiltaksnavn,
					tiltakstype = :tiltakstype,
				    navn = :navn,
					arrangor_id = :arrangor_id
				RETURNING *
				""".trimIndent(),
				sqlParameters(
					"id" to deltakerliste.id,
					"tiltaksnavn" to deltakerliste.tiltak.navn,
					"tiltakstype" to deltakerliste.tiltak.tiltakskode.name,
					"navn" to deltakerliste.navn,
					"arrangor_id" to deltakerliste.arrangorId,
				),
				rowMapper,
			).first()

		if (old == null) return RepositoryResult.Created(new)

		return RepositoryResult.Modified(new)
	}

	fun get(id: UUID): Deltakerliste? = template
		.query(
			"SELECT * from deltakerliste where id = :id",
			sqlParameters("id" to id),
			rowMapper,
		).firstOrNull()
}
