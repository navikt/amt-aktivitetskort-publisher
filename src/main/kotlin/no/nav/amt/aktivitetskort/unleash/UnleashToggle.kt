package no.nav.amt.aktivitetskort.unleash

import io.getunleash.Unleash
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode
import org.springframework.stereotype.Component

@Component
class UnleashToggle(
	private val unleashClient: Unleash,
) {
	fun erKometMasterForTiltakstype(tiltakskode: String): Boolean = tiltakstyperKometErMasterFor.any { it.name == tiltakskode } ||
		(unleashClient.isEnabled(ENABLE_KOMET_DELTAKERE) && tiltakstyperKometKanskjeErMasterFor.any { it.name == tiltakskode })

	fun erKometMasterForTiltakstype(tiltakskode: Tiltakskode): Boolean = erKometMasterForTiltakstype(tiltakskode.name)

	fun skalOppdatereForUendretDeltaker(): Boolean = unleashClient.isEnabled(OPPDATER_ALLE_AKTIVITETSKORT)

	fun skalLeseArenaDataForTiltakstype(tiltakskode: String): Boolean =
		unleashClient.isEnabled(LES_ARENA_DELTAKERE) && tiltakstyperKometKanLese.any { it.name == tiltakskode }

	fun skalLeseGjennomforing(tiltakskode: String): Boolean = tiltakstyperKometErMasterFor.any { it.name == tiltakskode } ||
		tiltakstyperKometKanLese.any { it.name == tiltakskode } ||
		tiltakstyperKometKanskjeErMasterFor.any { it.name == tiltakskode }

	companion object {
		const val ENABLE_KOMET_DELTAKERE = "amt.enable-komet-deltakere"
		const val OPPDATER_ALLE_AKTIVITETSKORT = "amt.oppdater-alle-aktivitetskort"
		const val LES_ARENA_DELTAKERE = "amt.les-arena-deltakere"

		private val tiltakstyperKometErMasterFor = setOf(
			Tiltakskode.ARBEIDSFORBEREDENDE_TRENING,
			Tiltakskode.OPPFOLGING,
			Tiltakskode.AVKLARING,
			Tiltakskode.ARBEIDSRETTET_REHABILITERING,
			Tiltakskode.DIGITALT_OPPFOLGINGSTILTAK,
			Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET,
			Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING,
			Tiltakskode.JOBBKLUBB,
			Tiltakskode.GRUPPE_FAG_OG_YRKESOPPLAERING,
		)

		private val tiltakstyperKometKanLese = setOf(
			Tiltakskode.ENKELTPLASS_ARBEIDSMARKEDSOPPLAERING,
			Tiltakskode.ENKELTPLASS_FAG_OG_YRKESOPPLAERING,
			Tiltakskode.HOYERE_UTDANNING,
		)

		private val tiltakstyperKometKanskjeErMasterFor = setOf(
			Tiltakskode.ARBEIDSMARKEDSOPPLAERING,
			Tiltakskode.NORSKOPPLAERING_GRUNNLEGGENDE_FERDIGHETER_FOV,
			Tiltakskode.STUDIESPESIALISERING,
			Tiltakskode.FAG_OG_YRKESOPPLAERING,
			Tiltakskode.HOYERE_YRKESFAGLIG_UTDANNING,
		)
	}
}
