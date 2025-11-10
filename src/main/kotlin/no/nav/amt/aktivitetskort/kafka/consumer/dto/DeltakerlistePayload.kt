package no.nav.amt.aktivitetskort.kafka.consumer.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode
import java.util.UUID

data class DeltakerlistePayload(
	val id: UUID,
	val navn: String? = null, // finnes kun for gruppetiltak
	val tiltakskode: String? = null, // skal gjøres non-nullable
	val tiltakstype: Tiltakstype? = null, // skal fjernes
	val arrangor: Arrangor,
) {
	// skal fjernes
	data class Tiltakstype(
		val tiltakskode: String,
	)

	data class Arrangor(
		val organisasjonsnummer: String,
	)

	// erstattes av tiltakskode senere
	@get:JsonIgnore
	val effectiveTiltakskode: String
		get() = tiltakskode ?: tiltakstype?.tiltakskode ?: throw IllegalStateException("Tiltakskode er ikke satt")

	fun toModel(arrangorId: UUID, navnTiltakstype: String): Deltakerliste = Deltakerliste(
		id = this.id,
		tiltak = Tiltak(navnTiltakstype, Tiltakskode.valueOf(effectiveTiltakskode)),
		navn = this.navn ?: navnTiltakstype,
		arrangorId = arrangorId,
	)
}
