package no.nav.amt.aktivitetskort.domain

import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakstype

data class Tiltak(
	val navn: String,
	val tiltakskode: Tiltakstype.Tiltakskode,
)

fun Tiltakstype.Tiltakskode.erTiltakmedDeltakelsesmengde() = when (this) {
	Tiltakstype.Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET -> true
	Tiltakstype.Tiltakskode.ARBEIDSFORBEREDENDE_TRENING -> true
	else -> false
}
