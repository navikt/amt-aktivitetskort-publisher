package no.nav.amt.aktivitetskort.service

import no.nav.amt.aktivitetskort.domain.AktivitetStatus
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.domain.Tag

object StatusMapping {

	private val PLANLEGGES_STATUS = listOf(
		DeltakerStatus.Type.SOKT_INN,
		DeltakerStatus.Type.VURDERES,
		DeltakerStatus.Type.VENTER_PA_OPPSTART,
		DeltakerStatus.Type.VENTELISTE,
	)

	private val GJENNOMFORER_STATUS = listOf(DeltakerStatus.Type.DELTAR)

	private val FULLFORT_STATUS = listOf(DeltakerStatus.Type.HAR_SLUTTET)

	private val AVBRUTT_STATUS = listOf(
		DeltakerStatus.Type.AVBRUTT,
		DeltakerStatus.Type.IKKE_AKTUELL,
		DeltakerStatus.Type.FEILREGISTRERT,
	)

	fun deltakerStatusTilAktivetStatus(deltakerStatus: DeltakerStatus.Type): Result<AktivitetStatus> {
		return when (deltakerStatus) {
			in PLANLEGGES_STATUS -> Result.success(AktivitetStatus.PLANLAGT)
			in FULLFORT_STATUS -> Result.success(AktivitetStatus.FULLFORT)
			in GJENNOMFORER_STATUS -> Result.success(AktivitetStatus.GJENNOMFORES)
			in AVBRUTT_STATUS -> Result.success(AktivitetStatus.AVBRUTT)
			else -> Result.failure(RuntimeException("Deltakerstatus ${deltakerStatus.name} kunne ikke mappes til aktivitetsstatus"))
		}
	}

	fun deltakerStatusTilEtikett(status: DeltakerStatus): Tag? {
		return when (status.type) {
			DeltakerStatus.Type.VENTER_PA_OPPSTART -> Tag(
				tekst = "Venter på oppstart",
				sentiment = Tag.Sentiment.WAITING,
				kode = Tag.Kode.VENTER_PA_OPPSTART,
			)
			DeltakerStatus.Type.SOKT_INN -> Tag(
				tekst = "Søkt inn",
				sentiment = Tag.Sentiment.NEUTRAL,
				kode = Tag.Kode.SOKT_INN,
			)
			DeltakerStatus.Type.VURDERES -> Tag(
				tekst = "Vurderes",
				sentiment = Tag.Sentiment.NEUTRAL,
				kode = Tag.Kode.VURDERES,
			)
			DeltakerStatus.Type.VENTELISTE -> Tag(
				tekst = "Venteliste",
				sentiment = Tag.Sentiment.NEUTRAL,
				kode = Tag.Kode.VENTELISTE,
			)
			DeltakerStatus.Type.IKKE_AKTUELL -> Tag(
				tekst = "Ikke aktuell",
				sentiment = Tag.Sentiment.NEGATIVE,
				kode = Tag.Kode.IKKE_AKTUELL,
			)
			DeltakerStatus.Type.DELTAR -> null
			DeltakerStatus.Type.HAR_SLUTTET -> null
			DeltakerStatus.Type.FEILREGISTRERT -> null
			DeltakerStatus.Type.AVBRUTT -> null
			DeltakerStatus.Type.PABEGYNT_REGISTRERING -> null
		}
	}
}
