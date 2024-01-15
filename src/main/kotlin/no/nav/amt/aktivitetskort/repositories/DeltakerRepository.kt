package no.nav.amt.aktivitetskort.repositories

import no.nav.amt.aktivitetskort.domain.AVSLUTTENDE_STATUSER
import no.nav.amt.aktivitetskort.domain.Deltaker
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import no.nav.amt.aktivitetskort.utils.sqlParameters
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class DeltakerRepository(
	private val template: NamedParameterJdbcTemplate,
) {
	val lanseringAktivitetsplan: LocalDate = LocalDate.of(2017, 12, 4)

	private val log = LoggerFactory.getLogger(javaClass)

	private val rowMapper = RowMapper { rs, _ ->
		DeltakerMedOffset(
			deltaker = Deltaker(
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
			),
			offset = rs.getLong("kafkaoffset"),
		)
	}

	fun upsert(deltaker: Deltaker, offset: Long): RepositoryResult<Deltaker> {
		val old = getDeltakerMedOffset(deltaker.id)

		if (old != null && old.offset > offset) {
			log.info("Har lagret melding med offset ${old.offset} for deltaker ${deltaker.id}, ignorerer offset $offset")
			return RepositoryResult.NoChange()
		}

		if (old == null && deltaker.status.type == DeltakerStatus.Type.FEILREGISTRERT) {
			return RepositoryResult.NoChange()
		}

		if (deltaker == old?.deltaker) return RepositoryResult.NoChange()

		if (deltaker.status.type in AVSLUTTENDE_STATUSER && deltaker.sluttdato?.isBefore(lanseringAktivitetsplan) == true &&
			!skalKorrigereTidligereDeltaker(old?.deltaker)
		) {
			log.info("Ignorerer deltaker som er avsluttet før aktivitetsplanen ble lansert, id ${deltaker.id}")
			return RepositoryResult.NoChange()
		}

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
				modified_at,
				kafkaoffset
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
				current_timestamp,
				:kafkaoffset
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
				modified_at = current_timestamp,
				kafkaoffset = :kafkaoffset
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
			"kafkaoffset" to offset,
		)

		val new = template.query(sql, parameters, rowMapper).first()

		if (old == null) return RepositoryResult.Created(new.deltaker)

		return RepositoryResult.Modified(new.deltaker)
	}

	fun get(id: UUID): Deltaker? = getDeltakerMedOffset(id)?.deltaker

	private fun skalKorrigereTidligereDeltaker(lagretDeltaker: Deltaker?): Boolean {
		return !(lagretDeltaker == null || lagretDeltaker.status.type in AVSLUTTENDE_STATUSER)
	}

	private fun getDeltakerMedOffset(id: UUID): DeltakerMedOffset? = template.query(
		"SELECT * from deltaker where id = :id",
		sqlParameters("id" to id),
		rowMapper,
	).firstOrNull()

	fun delete(id: UUID) {
		val sql = "DELETE FROM deltaker WHERE id = :id"
		val parameters = sqlParameters("id" to id)
		template.update(sql, parameters)
	}
}

private data class DeltakerMedOffset(
	val deltaker: Deltaker,
	val offset: Long,
)
