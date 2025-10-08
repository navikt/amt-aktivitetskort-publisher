package no.nav.amt.aktivitetskort.unleash

import io.getunleash.Unleash
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode
import org.springframework.stereotype.Component

@Component
class UnleashToggle(
	private val unleashClient: Unleash,
) {
	private val tiltakstyperKometAlltidErMasterFor = setOf(
		Tiltakskode.ARBEIDSFORBEREDENDE_TRENING,
		Tiltakskode.ARBEIDSRETTET_REHABILITERING,
		Tiltakskode.AVKLARING,
		Tiltakskode.OPPFOLGING,
		Tiltakskode.DIGITALT_OPPFOLGINGSTILTAK,
		Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET,
		Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING,
		Tiltakskode.GRUPPE_FAG_OG_YRKESOPPLAERING,
		Tiltakskode.JOBBKLUBB,
	)

	// her kan vi legge inn de neste tiltakstypene vi skal ta over
	private val tiltakstyperKometKanskjeErMasterFor = setOf(
		Tiltakskode.ENKELTPLASS_ARBEIDSMARKEDSOPPLAERING,
		Tiltakskode.ENKELTPLASS_FAG_OG_YRKESOPPLAERING,
		Tiltakskode.HOYERE_UTDANNING,
	)

	fun erKometMasterForTiltakstype(tiltakstype: Tiltakskode): Boolean = tiltakstype in tiltakstyperKometAlltidErMasterFor ||
		(unleashClient.isEnabled("amt.enable-komet-deltakere") && tiltakstype in tiltakstyperKometKanskjeErMasterFor)

	fun skalOppdatereForUendretDeltaker(): Boolean = unleashClient.isEnabled("amt.oppdater-alle-aktivitetskort")
}
