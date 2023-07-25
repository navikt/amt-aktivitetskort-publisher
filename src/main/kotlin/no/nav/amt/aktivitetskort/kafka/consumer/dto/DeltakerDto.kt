package no.nav.amt.aktivitetskort.kafka.consumer.dto

import no.nav.amt.aktivitetskort.domain.Deltaker
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import java.time.LocalDate
import java.util.UUID

data class DeltakerDto(
	val id: UUID,
	val personalia: DeltakerPersonaliaDto,
	val deltakerlisteId: UUID,
	val status: DeltakerStatusDto,
	val dagerPerUke: Float?,
	val prosentStilling: Double?,
	val oppstartsdato: LocalDate?,
	val sluttdato: LocalDate?,
	val deltarPaKurs: Boolean,
) {
	data class DeltakerPersonaliaDto(
		val personident: String,
	)

	data class DeltakerStatusDto(
		val type: DeltakerStatus.Type,
		val aarsak: DeltakerStatus.Aarsak?,
	)

	fun toModel() = Deltaker(
		id = id,
		personident = personalia.personident,
		deltakerlisteId = deltakerlisteId,
		status = DeltakerStatus(status.type, status.aarsak),
		dagerPerUke = dagerPerUke,
		prosentStilling = prosentStilling,
		oppstartsdato = oppstartsdato,
		sluttdato = sluttdato,
		deltarPaKurs = deltarPaKurs,
	)
}
