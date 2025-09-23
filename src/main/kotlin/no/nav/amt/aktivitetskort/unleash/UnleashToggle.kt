package no.nav.amt.aktivitetskort.unleash

import io.getunleash.Unleash
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakstype
import org.springframework.stereotype.Component

@Component
class UnleashToggle(
	private val unleashClient: Unleash,
) {
	private val tiltakstyperKometAlltidErMasterFors = listOf(
		Tiltakstype.ArenaKode.ARBFORB,
		Tiltakstype.ArenaKode.ARBRRHDAG,
		Tiltakstype.ArenaKode.AVKLARAG,
		Tiltakstype.ArenaKode.INDOPPFAG,
		Tiltakstype.ArenaKode.DIGIOPPARB,
		Tiltakstype.ArenaKode.VASV,
	)

	// her kan vi legge inn de neste tiltakstypene vi skal ta over
	private val tiltakstyperKometKanskjeErMasterFors = listOf(
		Tiltakstype.ArenaKode.GRUPPEAMO,
		Tiltakstype.ArenaKode.GRUFAGYRKE,
		Tiltakstype.ArenaKode.JOBBK,
	)

	fun erKometMasterForTiltakstype(tiltakstype: Tiltakstype.ArenaKode): Boolean = tiltakstype in tiltakstyperKometAlltidErMasterFors ||
		(unleashClient.isEnabled("amt.enable-komet-deltakere") && tiltakstype in tiltakstyperKometKanskjeErMasterFors)

	fun skalOppdatereForUendretDeltaker(): Boolean = unleashClient.isEnabled("amt.oppdater-alle-aktivitetskort")
}
