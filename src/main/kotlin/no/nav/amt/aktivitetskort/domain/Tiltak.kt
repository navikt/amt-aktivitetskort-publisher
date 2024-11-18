package no.nav.amt.aktivitetskort.domain

data class Tiltak(
	val navn: String,
	val type: Type,
) {
	enum class Type {
		INDOPPFAG,
		ARBFORB,
		AVKLARAG,
		VASV,
		ARBRRHDAG,
		DIGIOPPARB,
		JOBBK,
		GRUPPEAMO,
		GRUFAGYRKE,
	}
}

fun arenaKodeTilTiltakstype(type: String?) = when (type) {
	"ARBFORB" -> Tiltak.Type.ARBFORB
	"ARBRRHDAG" -> Tiltak.Type.ARBRRHDAG
	"AVKLARAG" -> Tiltak.Type.AVKLARAG
	"DIGIOPPARB" -> Tiltak.Type.DIGIOPPARB
	"GRUPPEAMO" -> Tiltak.Type.GRUPPEAMO
	"INDOPPFAG" -> Tiltak.Type.INDOPPFAG
	"JOBBK" -> Tiltak.Type.JOBBK
	"VASV" -> Tiltak.Type.VASV
	"GRUFAGYRKE" -> Tiltak.Type.GRUFAGYRKE
	else -> throw IllegalStateException("Mottatt ukjent tiltakstype: $type")
}

fun erTiltakmedDeltakelsesmendge(type: Tiltak.Type) = when (type) {
	Tiltak.Type.ARBFORB -> true
	Tiltak.Type.VASV -> true
	else -> false
}
