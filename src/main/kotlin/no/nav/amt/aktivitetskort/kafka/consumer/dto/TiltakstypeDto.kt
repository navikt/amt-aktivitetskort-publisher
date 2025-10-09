package no.nav.amt.aktivitetskort.kafka.consumer.dto

import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.aktivitetskort.kafka.TiltakstypeUtils.tiltakskodeAsEnumValue
import java.util.UUID

data class TiltakstypeDto(
	val id: UUID,
	val navn: String,
	val arenaKode: String,
	val tiltakskode: String,
) {
	fun toModel() = Tiltak(
		navn = this.navn,
		tiltakskode = tiltakskodeAsEnumValue(this.tiltakskode),
	)
}
