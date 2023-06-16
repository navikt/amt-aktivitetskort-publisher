package no.nav.amt.aktivitetskort.domain

enum class AktivitetStatus {
	FORSLAG,
	PLANLAGT,
	GJENNOMFORES,
	FULLFORT,
	AVBRUTT;

	companion object {
		val planleggesStatus = listOf(
			DeltakerStatus.Type.SOKT_INN,
			DeltakerStatus.Type.VURDERES,
			DeltakerStatus.Type.VENTER_PA_OPPSTART,
			DeltakerStatus.Type.VENTELISTE
		)

		val gjennomforerStatus = listOf(DeltakerStatus.Type.DELTAR)

		val fullfortStatus = listOf(DeltakerStatus.Type.HAR_SLUTTET)

		val avbruttStatus = listOf(DeltakerStatus.Type.AVBRUTT, DeltakerStatus.Type.IKKE_AKTUELL)
		fun from(deltakerStatus: DeltakerStatus.Type): AktivitetStatus {
			return when (deltakerStatus) {
				in planleggesStatus -> PLANLAGT
				in fullfortStatus -> FULLFORT
				in gjennomforerStatus -> GJENNOMFORES
				in avbruttStatus -> AVBRUTT
				else -> throw RuntimeException("Deltakerstatus ${deltakerStatus.name} kunne ikke mappes til aktivitetsstatus")
			}
		}
	}
}
