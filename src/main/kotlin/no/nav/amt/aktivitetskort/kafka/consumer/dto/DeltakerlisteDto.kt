package no.nav.amt.aktivitetskort.kafka.consumer.dto

import no.nav.amt.aktivitetskort.domain.Deltakerliste
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

	fun toModel() = Deltakerliste(
		this.id,
		this.tiltak.navn,
		this.navn,
		this.arrangor.id,
	)
}
