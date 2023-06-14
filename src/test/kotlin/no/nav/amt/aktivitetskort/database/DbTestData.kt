package no.nav.amt.aktivitetskort.database

import no.nav.amt.aktivitetskort.domain.Arrangor
import no.nav.amt.aktivitetskort.domain.Deltakerliste
import java.util.UUID

object DbTestData {

	fun arrangor(
		id: UUID = UUID.randomUUID(),
		navn: String = "navn"
	) = Arrangor(id, navn)

	fun deltakerliste(
		id: UUID = UUID.randomUUID(),
		tiltakstype: String = "tiltakstype",
		navn: String = "navn"
	) = Deltakerliste(id, tiltakstype, navn)
}
