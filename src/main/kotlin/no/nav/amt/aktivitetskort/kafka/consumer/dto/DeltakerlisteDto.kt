package no.nav.amt.aktivitetskort.kafka.consumer.dto

import java.time.LocalDate
import java.util.UUID

data class DeltakerlisteDto(
	val id: UUID,
	val navn: String,
	val arrangor: DeltakerlisteArrangorDto,
	val tiltak: TiltakDto,
	val erKurs: Boolean
) {
	data class TiltakDto(
		val navn: String,
		val type: String
	)

	data class DeltakerlisteArrangorDto(
		val id: UUID,
	)

	enum class DeltakerlisteStatus {
		APENT_FOR_INNSOK, GJENNOMFORES, AVSLUTTET
	}
}
