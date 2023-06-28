package no.nav.amt.aktivitetskort.service

import no.nav.amt.aktivitetskort.domain.AktivitetStatus
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.domain.Etikett

object StatusMapping {

	val PLANLEGGES_STATUS = listOf(
		DeltakerStatus.Type.SOKT_INN,
		DeltakerStatus.Type.VURDERES,
		DeltakerStatus.Type.VENTER_PA_OPPSTART,
		DeltakerStatus.Type.VENTELISTE,
	)

	val GJENNOMFORER_STATUS = listOf(DeltakerStatus.Type.DELTAR)

	val FULLFORT_STATUS = listOf(DeltakerStatus.Type.HAR_SLUTTET)

	val AVBRUTT_STATUS = listOf(DeltakerStatus.Type.AVBRUTT, DeltakerStatus.Type.IKKE_AKTUELL)

	fun deltakerStatusTilAktivetStatus(deltakerStatus: DeltakerStatus.Type): Result<AktivitetStatus> {
		return when (deltakerStatus) {
			in PLANLEGGES_STATUS -> Result.success(AktivitetStatus.PLANLAGT)
			in FULLFORT_STATUS -> Result.success(AktivitetStatus.FULLFORT)
			in GJENNOMFORER_STATUS -> Result.success(AktivitetStatus.GJENNOMFORES)
			in AVBRUTT_STATUS -> Result.success(AktivitetStatus.AVBRUTT)
			else -> Result.failure(RuntimeException("Deltakerstatus ${deltakerStatus.name} kunne ikke mappes til aktivitetsstatus"))
		}
	}

	fun deltakerStatusTilEtikett(status: DeltakerStatus): Etikett? {
		return when (status.type) {
			DeltakerStatus.Type.VENTER_PA_OPPSTART -> Etikett(Etikett.Kode.VENTER_PA_OPPSTART)
			DeltakerStatus.Type.SOKT_INN -> Etikett(Etikett.Kode.SOKT_INN)
			DeltakerStatus.Type.VURDERES -> Etikett(Etikett.Kode.VURDERES)
			DeltakerStatus.Type.VENTELISTE -> Etikett(Etikett.Kode.VENTELISTE)
			DeltakerStatus.Type.IKKE_AKTUELL -> Etikett(Etikett.Kode.IKKE_AKTUELL)
			DeltakerStatus.Type.DELTAR -> null
			DeltakerStatus.Type.HAR_SLUTTET -> null
			DeltakerStatus.Type.FEILREGISTRERT -> null
			DeltakerStatus.Type.AVBRUTT -> null
			DeltakerStatus.Type.PABEGYNT_REGISTRERING -> null
		}
	}
}
