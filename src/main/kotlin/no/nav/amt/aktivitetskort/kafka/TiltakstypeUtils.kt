package no.nav.amt.aktivitetskort.kafka

import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode

object TiltakstypeUtils {
	fun tiltakskodeErStottet(tiltakskode: String) = Tiltakskode.entries.any { it.name == tiltakskode }

	fun tiltakskodeAsEnumValue(tiltakskode: String): Tiltakskode = Tiltakskode.entries.find { it.name == tiltakskode }
		?: throw IllegalArgumentException("Ukjent tiltakskode: $tiltakskode")
}
