package no.nav.amt.aktivitetskort.repositories

import no.nav.amt.aktivitetskort.domain.Deltaker
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import no.nav.amt.aktivitetskort.utils.sqlParameters
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class DeltakerRepository(
	private val template: NamedParameterJdbcTemplate,
) {

	private val rowMapper = RowMapper { rs, _ ->
		Deltaker(
			id = UUID.fromString(rs.getString("id")),
			personident = rs.getString("personident"),
			deltakerlisteId = UUID.fromString(rs.getString("deltakerliste_id")),
			status = DeltakerStatus(
				type = DeltakerStatus.Type.valueOf(rs.getString("deltaker_status_type")),
				aarsak = rs.getString("deltaker_status_arsak")?.let { DeltakerStatus.Aarsak.valueOf(it) },
			),
			dagerPerUke = rs.getFloat("dager_per_uke"),
			prosentStilling = rs.getDouble("prosent_stilling"),
			oppstartsdato = rs.getDate("start_dato")?.toLocalDate(),
			sluttdato = rs.getDate("slutt_dato")?.toLocalDate(),
			deltarPaKurs = rs.getBoolean("deltar_pa_kurs"),
		)
	}

	fun upsert(deltaker: Deltaker): RepositoryResult<Deltaker> {
		val old = get(deltaker.id)
		if (deltaker == old) return RepositoryResult.NoChange()

		val sql = """
			insert into deltaker(
				id,
				personident,
				deltakerliste_id,
				deltaker_status_type,
				deltaker_status_arsak,
				dager_per_uke,
				prosent_stilling,
				start_dato,
				slutt_dato,
				deltar_pa_kurs,
				created_at,
				modified_at
			) values (
				:id,
				:personident,
				:deltakerliste_id,
				:deltaker_status_type,
				:deltaker_status_arsak,
				:dager_per_uke,
				:prosent_stilling,
				:start_dato,
				:slutt_dato,
				:deltar_pa_kurs,
				current_timestamp,
				current_timestamp
			) on conflict(id) do update set
				personident = :personident,
				deltakerliste_id = :deltakerliste_id,
				deltaker_status_type = :deltaker_status_type,
				deltaker_status_arsak = :deltaker_status_arsak,
				dager_per_uke = :dager_per_uke,
				prosent_stilling = :prosent_stilling,
				start_dato = :start_dato,
				slutt_dato = :slutt_dato,
				deltar_pa_kurs = :deltar_pa_kurs,
				modified_at = current_timestamp
			returning *
		""".trimIndent()
		val parameters = sqlParameters(
			"id" to deltaker.id,
			"personident" to deltaker.personident,
			"deltakerliste_id" to deltaker.deltakerlisteId,
			"deltaker_status_type" to deltaker.status.type.name,
			"deltaker_status_arsak" to deltaker.status.aarsak?.name,
			"dager_per_uke" to deltaker.dagerPerUke,
			"prosent_stilling" to deltaker.prosentStilling,
			"start_dato" to deltaker.oppstartsdato,
			"slutt_dato" to deltaker.sluttdato,
			"deltar_pa_kurs" to deltaker.deltarPaKurs,
		)

		val new = template.query(sql, parameters, rowMapper).first()

		if (old == null) return RepositoryResult.Created(new)

		return RepositoryResult.Modified(new)
	}

	fun get(id: UUID): Deltaker? = template.query(
		"SELECT * from deltaker where id = :id",
		sqlParameters("id" to id),
		rowMapper,
	).firstOrNull()
}
