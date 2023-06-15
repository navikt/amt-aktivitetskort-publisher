package no.nav.amt.aktivitetskort.kafka.consumer.dto

import java.util.UUID

data class ArrangorDto(
	val id: UUID,
	val navn: String,
)
