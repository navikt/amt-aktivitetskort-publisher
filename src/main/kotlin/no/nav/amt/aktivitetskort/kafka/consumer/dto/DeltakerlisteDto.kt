package no.nav.amt.aktivitetskort.kafka.consumer.dto

import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.aktivitetskort.domain.arenaKodeTilArenaType
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakstype
import java.util.UUID

data class DeltakerlisteDto(
	val id: UUID,
	val navn: String,
	val tiltakstypeDto: TiltakstypeDto,
	val virksomhetsnummer: String,
) {
	data class TiltakstypeDto(
		val id: UUID,
		val navn: String,
		val arenaKode: String,
		val tiltakskode: Tiltakstype.Tiltakskode,
	) {
		fun toModel() = Tiltak(
			this.navn,
			arenaKodeTilArenaType(this.arenaKode),
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
		tiltak = this.tiltakstypeDto.toModel(),
		navn = this.navn,
		arrangorId = arrangorId,
	)
}
