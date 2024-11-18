package no.nav.amt.aktivitetskort.kafka.consumer.dto

import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.aktivitetskort.domain.arenaKodeTilTiltakstype
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
			this.navn,
			arenaKodeTilTiltakstype(this.arenaKode),
		)

		fun erStottet() = this.arenaKode in setOf(
			"INDOPPFAG",
			"ARBFORB",
			"AVKLARAG",
			"VASV",
			"ARBRRHDAG",
			"DIGIOPPARB",
			"JOBBK",
			"GRUPPEAMO",
			"GRUFAGYRKE",
		)
	}

	fun toModel(arrangorId: UUID) = Deltakerliste(
		id = this.id,
		tiltak = this.tiltakstype.toModel(),
		navn = this.navn,
		arrangorId = arrangorId,
	)
}
