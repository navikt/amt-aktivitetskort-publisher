package no.nav.amt.aktivitetskort.kafka.consumer.dto

import no.nav.amt.aktivitetskort.domain.Arrangor
import java.util.UUID

data class ArrangorDto(
	val id: UUID,
	val organisasjonsnummer: String,
	val navn: String,
) {
	fun toModel() = Arrangor(
		this.id,
		this.organisasjonsnummer,
		this.navn,
	)
}
