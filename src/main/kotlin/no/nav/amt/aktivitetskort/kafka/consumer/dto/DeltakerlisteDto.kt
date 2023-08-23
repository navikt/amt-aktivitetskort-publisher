package no.nav.amt.aktivitetskort.kafka.consumer.dto

import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.aktivitetskort.domain.arenaKodeTilTiltakstype
import no.nav.amt.aktivitetskort.domain.cleanTiltaksnavn
import java.util.UUID

data class DeltakerlisteDto(
	val id: UUID,
	val navn: String,
	val tiltakstype: Tiltakstype,
	val virksomhetsnummer: String,
) {
	data class Tiltakstype(
		val navn: String,
		val arenaKode: String,
	) {
		fun toModel() = Tiltak(
			cleanTiltaksnavn(this.navn),
			arenaKodeTilTiltakstype(this.arenaKode),
		)
	}

	fun toModel(arrangorId: UUID) = Deltakerliste(
		id = this.id,
		tiltak = this.tiltakstype.toModel(),
		navn = this.navn,
		arrangorId = arrangorId,
	)
}
