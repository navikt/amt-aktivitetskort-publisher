package no.nav.amt.aktivitetskort.domain

import java.util.UUID

data class Deltakerliste(
	val id: UUID,
	val tiltaksnavn: String,
	val navn: String,
	val arrangorId: UUID,
)
