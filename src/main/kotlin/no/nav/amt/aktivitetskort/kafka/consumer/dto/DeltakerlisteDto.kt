package no.nav.amt.aktivitetskort.kafka.consumer.dto

import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakstype
import java.util.UUID

data class DeltakerlisteDto(
	val id: UUID,
	val navn: String,
	val tiltakstype: TiltakstypeDto,
	val virksomhetsnummer: String,
) {
	data class TiltakstypeDto(
		val id: UUID,
		val navn: String,
		val arenaKode: String,
		val tiltakskode: String,
	) {
		fun toModel() = Tiltak(
			this.navn,
			tiltakskodeToTiltaksEnum(this.tiltakskode),
		)

		fun erStottet() = this.tiltakskode in setOf(
			"OPPFOLGING",
			"ARBEIDSFORBEREDENDE_TRENING",
			"AVKLARING",
			"VARIG_TILRETTELAGT_ARBEID_SKJERMET",
			"ARBEIDSRETTET_REHABILITERING",
			"DIGITALT_OPPFOLGINGSTILTAK",
			"JOBBKLUBB",
			"GRUPPE_ARBEIDSMARKEDSOPPLAERING",
			"GRUPPE_FAG_OG_YRKESOPPLAERING",
		)

		fun tiltakskodeToTiltaksEnum(tiltakskode: String) = when (tiltakskode) {
			"OPPFOLGING" -> Tiltakstype.Tiltakskode.OPPFOLGING
			"ARBEIDSFORBEREDENDE_TRENING" -> Tiltakstype.Tiltakskode.ARBEIDSFORBEREDENDE_TRENING
			"AVKLARING" -> Tiltakstype.Tiltakskode.AVKLARING
			"VARIG_TILRETTELAGT_ARBEID_SKJERMET" -> Tiltakstype.Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET
			"ARBEIDSRETTET_REHABILITERING" -> Tiltakstype.Tiltakskode.ARBEIDSRETTET_REHABILITERING
			"DIGITALT_OPPFOLGINGSTILTAK" -> Tiltakstype.Tiltakskode.DIGITALT_OPPFOLGINGSTILTAK
			"JOBBKLUBB" -> Tiltakstype.Tiltakskode.JOBBKLUBB
			"GRUPPE_ARBEIDSMARKEDSOPPLAERING" -> Tiltakstype.Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING
			"GRUPPE_FAG_OG_YRKESOPPLAERING" -> Tiltakstype.Tiltakskode.GRUPPE_FAG_OG_YRKESOPPLAERING
			else -> throw IllegalArgumentException("Unknown tiltakskode: $tiltakskode")
		}
	}

	fun toModel(arrangorId: UUID) = Deltakerliste(
		id = this.id,
		tiltak = this.tiltakstype.toModel(),
		navn = this.navn,
		arrangorId = arrangorId,
	)
}
