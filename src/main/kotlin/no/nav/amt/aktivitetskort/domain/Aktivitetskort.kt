package no.nav.amt.aktivitetskort.domain

import java.text.DecimalFormat
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
		fun lagTittel(deltakerliste: Deltakerliste, arrangor: Arrangor) = when (deltakerliste.tiltak.type) {
			Tiltak.Type.DIGITALT_OPPFOELGINGSTILTAK -> "Digital oppfølging hos ${arrangor.navn}"
			Tiltak.Type.JOBBKLUBB -> "Jobbsøkerkurs hos ${arrangor.navn}"
			Tiltak.Type.ARBEIDSMARKEDSOPPLAERING -> "Kurs: ${deltakerliste.navn}"
			else -> "${deltakerliste.tiltak.navn} hos ${arrangor.navn}"
		}

		fun lagDetaljer(deltaker: Deltaker, deltakerliste: Deltakerliste, arrangor: Arrangor): List<Detalj> {
			val detaljer = mutableListOf<Detalj>()

			detaljer.add(Detalj("Status for deltakelse", deltaker.status.display()))

			if (erTiltakmedDeltakelsesmendge(deltakerliste.tiltak.type)) {
				deltakelseMengdeDetalj(deltaker)?.let { detaljer.add(it) }
			}

			detaljer.add(Detalj("Arrangør", arrangor.navn))

			return detaljer
		}

		private fun deltakelseMengdeDetalj(deltaker: Deltaker): Detalj? {
			val harDagerPerUke = deltaker.dagerPerUke?.let { it in 1.0f..5.0f } == true
			val harProsentStilling = deltaker.prosentStilling?.let { it in 1.0..100.0 } == true

			val label = "Deltakelsesmengde"

			fun fmtProsent(pct: Double) = "${DecimalFormat("#.#").format(pct)}%"
			fun fmtDager(antall: Float) = "${DecimalFormat("#.#").format(antall)} ${if (antall == 1.0f) "dag" else "dager"} i uka"

			return when {
				!harProsentStilling && !harDagerPerUke -> null
				deltaker.prosentStilling == 100.0 || !harDagerPerUke ->
					deltaker.prosentStilling?.let { Detalj(label, fmtProsent(it)) }

				!harProsentStilling ->
					deltaker.dagerPerUke?.let { Detalj(label, fmtDager(it)) }

				else ->
					Detalj(label, "${fmtProsent(deltaker.prosentStilling!!)} ${fmtDager(deltaker.dagerPerUke!!)}")
			}
		}
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
