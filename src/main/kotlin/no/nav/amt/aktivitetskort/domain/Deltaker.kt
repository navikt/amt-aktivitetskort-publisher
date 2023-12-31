package no.nav.amt.aktivitetskort.domain

import java.time.LocalDate
import java.util.UUID

data class Deltaker(
	val id: UUID,
	val personident: String,
	val deltakerlisteId: UUID,
	val status: DeltakerStatus,
	val dagerPerUke: Float?,
	val prosentStilling: Double?,
	val oppstartsdato: LocalDate?,
	val sluttdato: LocalDate?,
	val deltarPaKurs: Boolean,
)

data class DeltakerStatus(
	val type: Type,
	val aarsak: Aarsak?,
) {
	enum class Type {
		VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL, FEILREGISTRERT,
		SOKT_INN, VURDERES, VENTELISTE, AVBRUTT, FULLFORT, // kursstatuser
		PABEGYNT_REGISTRERING,
		UTKAST_TIL_PAMELDING,
		AVBRUTT_UTKAST,
		;

		fun display() = when (this) {
			VENTER_PA_OPPSTART -> "Venter på oppstart"
			DELTAR -> "Deltar"
			HAR_SLUTTET -> "Har sluttet"
			IKKE_AKTUELL -> "Ikke aktuell"
			FEILREGISTRERT -> "Feilregistrert"
			SOKT_INN -> "Søkt inn"
			VURDERES -> "Vurderes"
			VENTELISTE -> "Venteliste"
			AVBRUTT -> "Avbrutt"
			FULLFORT -> "Fullført"
			PABEGYNT_REGISTRERING -> "Søkt inn"
			UTKAST_TIL_PAMELDING -> "Utkast til påmelding"
			AVBRUTT_UTKAST -> "Avbrutt utkast"
		}
	}

	enum class Aarsak {
		SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, FIKK_IKKE_PLASS, IKKE_MOTT, ANNET, AVLYST_KONTRAKT;

		fun display() = when (this) {
			SYK -> "Syk"
			FATT_JOBB -> "Fått jobb"
			TRENGER_ANNEN_STOTTE -> "Trenger annen støtte"
			FIKK_IKKE_PLASS -> "Fikk ikke plass"
			IKKE_MOTT -> "Møter ikke opp"
			ANNET -> "Annet"
			AVLYST_KONTRAKT -> "Avlyst kontrakt"
		}
	}

	fun display(): String {
		if (this.aarsak != null) {
			return "${this.type.display()} - årsak: ${this.aarsak.display().lowercase()}"
		}
		return this.type.display()
	}
}
