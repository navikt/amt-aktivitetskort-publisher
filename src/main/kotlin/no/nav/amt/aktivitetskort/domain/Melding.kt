package no.nav.amt.aktivitetskort.domain

import java.time.ZonedDateTime
import java.util.UUID

data class Melding(
	val deltakerId: UUID,
	val deltakerlisteId: UUID,
	val arrangorId: UUID,
	val melding: Aktivitetskort,
	val createdAt: ZonedDateTime,
	val modifiedAt: ZonedDateTime
)
