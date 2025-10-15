package no.nav.amt.aktivitetskort.kafka.consumer.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode
import java.util.UUID

data class DeltakerlistePayload(
	val id: UUID,
	val navn: String? = null, // finnes kun for gruppetiltak
	val tiltakstype: Tiltakstype,
	val virksomhetsnummer: String? = null, // finnes kun for v1
	val arrangor: Arrangor? = null, // finnes kun for v2
) {
	data class Tiltakstype(
		val tiltakskode: String,
		val navn: String = "TODO", // vil komme etterhvert fra Mulighetsrommet
	) {
		fun erStottet() = Tiltakskode.entries.any { it.name == tiltakskode }
	}

	data class Arrangor(
		val organisasjonsnummer: String,
	)

	@get:JsonIgnore
	val organisasjonsnummer: String
		get() = setOfNotNull(arrangor?.organisasjonsnummer, virksomhetsnummer)
			.firstOrNull()
			?: throw IllegalStateException("Virksomhetsnummer mangler")

	fun toModel(arrangorId: UUID) = Deltakerliste(
		id = this.id,
		tiltak = Tiltak(this.tiltakstype.navn, Tiltakskode.valueOf(this.tiltakstype.tiltakskode)),
		navn = this.navn ?: tiltakstype.navn,
		arrangorId = arrangorId,
	)
}
