package no.nav.amt.aktivitetskort.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Objects
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
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Aktivitetskort) return false

		// Sammenligner ikke endretTidspunkt for de vil ofte være forskjellig, selv om alt annet er likt
		return id == other.id &&
			personident == other.personident &&
			tittel == other.tittel &&
			aktivitetStatus == other.aktivitetStatus &&
			startDato == other.startDato &&
			sluttDato == other.sluttDato &&
			beskrivelse == other.beskrivelse &&
			endretAv == other.endretAv &&
			avtaltMedNav == other.avtaltMedNav &&
			oppgave == other.oppgave &&
			handlinger == other.handlinger &&
			detaljer == other.detaljer &&
			etiketter == other.etiketter
	}

	override fun hashCode(): Int {
		return Objects.hash(
			id,
			personident,
			tittel,
			aktivitetStatus,
			startDato,
			sluttDato,
			beskrivelse,
			endretAv,
			avtaltMedNav,
			oppgave,
			handlinger,
			detaljer,
			etiketter,
		)
	}

	companion object {
		fun lagTittel(tiltakstype: String, arrangorNavn: String) = "$tiltakstype hos $arrangorNavn"
	}
}

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
