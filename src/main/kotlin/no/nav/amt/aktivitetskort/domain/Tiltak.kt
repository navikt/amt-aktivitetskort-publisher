package no.nav.amt.aktivitetskort.domain

import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode

data class Tiltak(
    val navn: String,
    val tiltakskode: Tiltakskode,
)

fun Tiltakskode.erTiltakmedDeltakelsesmengde() = when (this) {
    Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET,
    Tiltakskode.TILPASSET_JOBBSTOTTE,
    Tiltakskode.ARBEIDSFORBEREDENDE_TRENING,
    -> true
    else -> false
}
