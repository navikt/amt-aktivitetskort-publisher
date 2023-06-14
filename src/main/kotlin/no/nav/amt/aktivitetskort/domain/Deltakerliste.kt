package no.nav.amt.aktivitetskort.domain

import java.util.UUID

data class Deltakerliste(
	val id: UUID,
	val tiltakstype: String,
	val navn: String
)
