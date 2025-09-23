package no.nav.amt.aktivitetskort.domain

import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakstype

data class Tiltak(
	val navn: String,
	val arenaKode: Tiltakstype.ArenaKode,
	val tiltakskode: Tiltakstype.Tiltakskode = arenaKode.tilTiltakskode(),
)

fun arenaKodeTilArenaType(type: String?) = when (type) {
	"ARBFORB" -> Tiltakstype.ArenaKode.ARBFORB
	"ARBRRHDAG" -> Tiltakstype.ArenaKode.ARBRRHDAG
	"AVKLARAG" -> Tiltakstype.ArenaKode.AVKLARAG
	"DIGIOPPARB" -> Tiltakstype.ArenaKode.DIGIOPPARB
	"GRUPPEAMO" -> Tiltakstype.ArenaKode.GRUPPEAMO
	"INDOPPFAG" -> Tiltakstype.ArenaKode.INDOPPFAG
	"JOBBK" -> Tiltakstype.ArenaKode.JOBBK
	"VASV" -> Tiltakstype.ArenaKode.VASV
	"GRUFAGYRKE" -> Tiltakstype.ArenaKode.GRUFAGYRKE
	else -> throw IllegalStateException("Mottatt ukjent tiltakstype: $type")
}

fun Tiltakstype.ArenaKode.tilTiltakskode() = when (this) {
	Tiltakstype.ArenaKode.ARBFORB -> Tiltakstype.Tiltakskode.ARBEIDSFORBEREDENDE_TRENING
	Tiltakstype.ArenaKode.ARBRRHDAG -> Tiltakstype.Tiltakskode.ARBEIDSRETTET_REHABILITERING
	Tiltakstype.ArenaKode.AVKLARAG -> Tiltakstype.Tiltakskode.AVKLARING
	Tiltakstype.ArenaKode.DIGIOPPARB -> Tiltakstype.Tiltakskode.DIGITALT_OPPFOLGINGSTILTAK
	Tiltakstype.ArenaKode.GRUPPEAMO -> Tiltakstype.Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING
	Tiltakstype.ArenaKode.GRUFAGYRKE -> Tiltakstype.Tiltakskode.GRUPPE_FAG_OG_YRKESOPPLAERING
	Tiltakstype.ArenaKode.INDOPPFAG -> Tiltakstype.Tiltakskode.OPPFOLGING
	Tiltakstype.ArenaKode.VASV -> Tiltakstype.Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET
	Tiltakstype.ArenaKode.JOBBK -> Tiltakstype.Tiltakskode.JOBBKLUBB
}

fun Tiltakstype.ArenaKode.erTiltakmedDeltakelsesmengde() = when (this) {
	Tiltakstype.ArenaKode.ARBFORB -> true
	Tiltakstype.ArenaKode.VASV -> true
	else -> false
}
