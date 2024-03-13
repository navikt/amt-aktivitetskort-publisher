package no.nav.amt.aktivitetskort.domain

import java.time.LocalDate
import java.util.Objects
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
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Deltaker) return false

		return id == other.id &&
			personident == other.personident &&
			deltakerlisteId == other.deltakerlisteId &&
			status == other.status &&
			isEqual(dagerPerUke, other.dagerPerUke) &&
			isEqual(prosentStilling, other.prosentStilling) &&
			oppstartsdato == other.oppstartsdato &&
			sluttdato == other.sluttdato &&
			deltarPaKurs == other.deltarPaKurs
	}

	private fun isEqual(dagerPerUke: Float?, otherDagerPerUke: Float?): Boolean {
		return (dagerPerUke == otherDagerPerUke) ||
			(dagerPerUke == null && otherDagerPerUke == 0.0F) ||
			(dagerPerUke == 0.0F && otherDagerPerUke == null)
	}

	private fun isEqual(prosentStilling: Double?, otherProsentStilling: Double?): Boolean {
		return (prosentStilling == otherProsentStilling) ||
			(prosentStilling == null && otherProsentStilling == 0.0) ||
			(prosentStilling == 0.0 && otherProsentStilling == null)
	}

	override fun hashCode(): Int {
		return Objects.hash(
			id,
			personident,
			deltakerlisteId,
			status,
			dagerPerUke,
			prosentStilling,
			oppstartsdato,
			sluttdato,
			deltarPaKurs,
		)
	}
}

data class DeltakerStatus(
	val type: Type,
	val aarsak: Aarsak?,
) {
	enum class Type {
		VENTER_PA_OPPSTART,
		DELTAR,
		HAR_SLUTTET,
		IKKE_AKTUELL,
		FEILREGISTRERT,
		SOKT_INN,
		VURDERES,
		VENTELISTE,
		AVBRUTT,
		FULLFORT,
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
		SYK,
		FATT_JOBB,
		TRENGER_ANNEN_STOTTE,
		FIKK_IKKE_PLASS,
		IKKE_MOTT,
		ANNET,
		AVLYST_KONTRAKT,
		;

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

val AVSLUTTENDE_STATUSER = listOf(
	DeltakerStatus.Type.HAR_SLUTTET,
	DeltakerStatus.Type.IKKE_AKTUELL,
	DeltakerStatus.Type.FEILREGISTRERT,
	DeltakerStatus.Type.AVBRUTT,
	DeltakerStatus.Type.FULLFORT,
	DeltakerStatus.Type.AVBRUTT_UTKAST,
)
