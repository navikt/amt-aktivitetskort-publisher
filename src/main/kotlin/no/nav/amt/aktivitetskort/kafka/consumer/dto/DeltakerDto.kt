package no.nav.amt.aktivitetskort.kafka.consumer.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID


data class DeltakerDto(
	val id: UUID,
	val personalia: DeltakerPersonaliaDto,
	val deltakerlisteId: UUID,
	val status: DeltakerStatusDto,
	val dagerPerUke: Int?,
	val prosentStilling: Double?,
	val oppstartsdato: LocalDate?,
	val sluttdato: LocalDate?,
	val innsoktDato: LocalDate,
	val deltarPaKurs: Boolean
) {
	data class DeltakerPersonaliaDto(
		val personident: String,
	)

	data class DeltakerStatusDto(
		val type: DeltakerStatus,
	) {
		enum class DeltakerStatus {
			VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL, FEILREGISTRERT,
			SOKT_INN, VURDERES, VENTELISTE, AVBRUTT, // kursstatuser
			PABEGYNT_REGISTRERING
		}
	}

}
