package no.nav.amt.aktivitetskort.domain

data class Tiltak(
	val navn: String,
	val type: Type,
) {
	enum class Type {
		ARBEIDSFORBEREDENDE_TRENING,
		ARBEIDSRETTET_REHABILITERING,
		AVKLARING,
		DIGITALT_OPPFOELGINGSTILTAK,
		ARBEIDSMARKEDSOPPLAERING,
		JOBBKLUBB,
		OPPFOELGING,
		VARIG_TILRETTELAGT_ARBEID,
		UKJENT,
	}
}

fun arenaKodeTilTiltakstype(type: String?) = when (type) {
	"ARBFORB" -> Tiltak.Type.ARBEIDSFORBEREDENDE_TRENING
	"ARBRRHDAG" -> Tiltak.Type.ARBEIDSRETTET_REHABILITERING
	"AVKLARAG" -> Tiltak.Type.AVKLARING
	"DIGIOPPARB" -> Tiltak.Type.DIGITALT_OPPFOELGINGSTILTAK
	"GRUPPEAMO" -> Tiltak.Type.ARBEIDSMARKEDSOPPLAERING
	"INDOPPFAG" -> Tiltak.Type.OPPFOELGING
	"JOBBK" -> Tiltak.Type.JOBBKLUBB
	"VASV" -> Tiltak.Type.VARIG_TILRETTELAGT_ARBEID
	else -> Tiltak.Type.UKJENT
}

fun cleanTiltaksnavn(navn: String) = when (navn) {
	"Arbeidsforberedende trening (AFT)" -> "Arbeidsforberedende trening"
	"Arbeidsrettet rehabilitering (dag)" -> "Arbeidsrettet rehabilitering"
	"Digitalt oppfølgingstiltak for arbeidsledige (jobbklubb)" -> "Digitalt oppfølgingstiltak"
	"Gruppe AMO" -> "Arbeidsmarkedsopplæring"
	"Varig tilrettelagt arbeid i skjermet virksomhet" -> "Varig tilrettelagt arbeid"
	else -> navn
}
