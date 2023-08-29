package no.nav.amt.aktivitetskort.controller

import jakarta.servlet.http.HttpServletRequest
import no.nav.amt.aktivitetskort.client.AmtArenaAclClient
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/internal")
class InternalController(
	private val amtArenaAclClient: AmtArenaAclClient,
) {

	@Unprotected
	@GetMapping("/arenaId/{amtId}")
	fun getArenaIdForAmtId(
		servlet: HttpServletRequest,
		@PathVariable("amtId") amtId: UUID,
	): String {
		if (isInternal(servlet)) {
			return amtArenaAclClient.getArenaIdForAmtId(amtId)
		}

		throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
	}

	private fun isInternal(servlet: HttpServletRequest) = servlet.remoteAddr == "127.0.0.1"
}
