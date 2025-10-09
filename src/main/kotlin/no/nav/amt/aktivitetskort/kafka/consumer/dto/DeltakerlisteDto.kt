package no.nav.amt.aktivitetskort.kafka.consumer.dto

import no.nav.amt.aktivitetskort.domain.Deltakerliste
import java.util.UUID

data class DeltakerlisteDto(
	val id: UUID,
	val navn: String,
	val tiltakstype: TiltakstypeDto,
	val virksomhetsnummer: String,
) {
	fun toModel(arrangorId: UUID) = Deltakerliste(
		id = this.id,
		tiltak = this.tiltakstype.toModel(),
		navn = this.navn,
		arrangorId = arrangorId,
	)
}
