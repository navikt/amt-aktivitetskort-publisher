package no.nav.amt.aktivitetskort.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Aktivitetskort(
	val id: UUID,
	val personident: String,
	val tittel: String,
	val aktivitetStatus: AktivitetStatus,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val beskrivelse: String?,
	val endretAv: EndretAv,
	val endretTidspunkt: LocalDateTime,
	val avtaltMedNav: Boolean,
	val oppgave: OppgaveWrapper? = null,
	val handlinger: List<Handling>? = null,
	val detaljer: List<Detalj>,
	val etiketter: List<Etikett>,
)

data class Etikett(
	val kode: Kode,
) {
	enum class Kode {
		SOKT_INN, VURDERES, VENTER_PA_OPPSTART, VENTELISTE, IKKE_AKTUELL
	}
}

data class Detalj(
	val label: String,
	val verdi: String,
)

data class Handling(
	val tekst: String,
	val subtekst: String,
	val url: String,
	val lenkeType: LenkeType,
)

enum class LenkeType {
	EKSTERN,
	INTERN,
	FELLES,
}

data class OppgaveWrapper(
	val ekstern: Oppgave?,
	val intern: Oppgave?,
)

data class Oppgave(
	val tekst: String,
	val subtekst: String,
	val url: String,
)

data class EndretAv(
	val ident: String,
	val identType: IdentType,
)

enum class IdentType {
	ARENAIDENT,
	NAVIDENT,
	PERSONBRUKER,
	TILTAKSARRAGOER,
	ARBEIDSGIVER,
	SYSTEM,
}
