package no.nav.amt.aktivitetskort.unleash

import io.getunleash.Unleash
import no.nav.amt.aktivitetskort.domain.Tiltak
import org.springframework.stereotype.Component

@Component
class UnleashToggle(
	private val unleashClient: Unleash,
) {
	private val tiltakstyperKometAlltidErMasterFor = listOf(
		Tiltak.Type.ARBFORB,
	)

	// her kan vi legge inn de neste tiltakstypene vi skal ta over
	private val tiltakstyperKometKanskjeErMasterFor = emptyList<Tiltak.Type>()

	fun erKometMasterForTiltakstype(tiltakstype: Tiltak.Type): Boolean {
		return tiltakstype in tiltakstyperKometAlltidErMasterFor ||
			(unleashClient.isEnabled("amt.enable-komet-deltakere") && tiltakstype in tiltakstyperKometKanskjeErMasterFor)
	}

	fun skalOppdatereForUendretDeltaker(): Boolean {
		return unleashClient.isEnabled("amt.oppdater-alle-aktivitetskort")
	}
}
