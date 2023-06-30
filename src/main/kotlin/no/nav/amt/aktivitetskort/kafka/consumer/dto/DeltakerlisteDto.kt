package no.nav.amt.aktivitetskort.kafka.consumer.dto

import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.aktivitetskort.domain.arenaKodeTilTiltakstype
import no.nav.amt.aktivitetskort.domain.cleanTiltaksnavn
import java.util.UUID

data class DeltakerlisteDto(
	val id: UUID,
	val navn: String,
	val arrangor: DeltakerlisteArrangorDto,
	val tiltak: TiltakDto,
) {
	data class TiltakDto(
		val navn: String,
		val type: String,
	) {
		fun toModel() = Tiltak(
			cleanTiltaksnavn(this.navn),
			arenaKodeTilTiltakstype(this.type),
		)
	}

	data class DeltakerlisteArrangorDto(
		val id: UUID,
	)

	fun toModel() = Deltakerliste(
		this.id,
		this.tiltak.toModel(),
		this.navn,
		this.arrangor.id,
	)
}
