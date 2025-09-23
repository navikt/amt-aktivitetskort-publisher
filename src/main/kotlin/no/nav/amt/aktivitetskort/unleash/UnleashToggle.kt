package no.nav.amt.aktivitetskort.unleash

import io.getunleash.Unleash
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakstype
import org.springframework.stereotype.Component

@Component
class UnleashToggle(
	private val unleashClient: Unleash,
) {
	private val tiltakstyperKometAlltidErMasterFor = listOf(
		Tiltakstype.Tiltakskode.ARBEIDSFORBEREDENDE_TRENING,
		Tiltakstype.Tiltakskode.ARBEIDSRETTET_REHABILITERING,
		Tiltakstype.Tiltakskode.AVKLARING,
		Tiltakstype.Tiltakskode.OPPFOLGING,
		Tiltakstype.Tiltakskode.DIGITALT_OPPFOLGINGSTILTAK,
		Tiltakstype.Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET,
    Tiltakstype.Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING,
		Tiltakstype.Tiltakskode.GRUPPE_FAG_OG_YRKESOPPLAERING,
    Tiltakstype.Tiltakskode.JOBBKLUBB,
	)

	// her kan vi legge inn de neste tiltakstypene vi skal ta over
	private val tiltakstyperKometKanskjeErMasterFor = emptyList<Tiltakstype.Tiltakskode>()

	fun erKometMasterForTiltakstype(tiltakstype: Tiltakstype.Tiltakskode): Boolean = tiltakstype in tiltakstyperKometAlltidErMasterFor ||
		(unleashClient.isEnabled("amt.enable-komet-deltakere") && tiltakstype in tiltakstyperKometKanskjeErMasterFor)

	fun skalOppdatereForUendretDeltaker(): Boolean = unleashClient.isEnabled("amt.oppdater-alle-aktivitetskort")
}
