package no.nav.amt.aktivitetskort.internal

import jakarta.servlet.http.HttpServletRequest
import no.nav.amt.aktivitetskort.kafka.producer.AktivitetskortProducer
import no.nav.amt.aktivitetskort.service.AktivitetskortService
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/internal")
class InternalAPI(
	private val aktivitetskortService: AktivitetskortService,
	private val aktivitetskortProducer: AktivitetskortProducer,
) {
	private val log = LoggerFactory.getLogger(InternalAPI::class.java)

	@Unprotected
	@GetMapping("/publiser/{deltakerId}")
	fun publiserAktivitetskortForDeltaker(
		servlet: HttpServletRequest,
		@PathVariable("deltakerId") deltakerId: UUID,
	) {
		if (isInternal(servlet)) {
			val aktivitetskort = aktivitetskortService.lagAktivitetskort(deltakerId)
			aktivitetskortProducer.send(aktivitetskort)
			log.info("Publiserte aktivitetskort for deltaker med id $deltakerId")
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	@Unprotected
	@GetMapping("/resend/{deltakerId}")
	fun resendSistSendteMelding(
		servlet: HttpServletRequest,
		@PathVariable("deltakerId") deltakerId: UUID,
	) {
		if (isInternal(servlet)) {
			val aktivitetskort = aktivitetskortService
				.getSisteMeldingForDeltaker(
					deltakerId,
				)?.aktivitetskort ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke melding")
			aktivitetskortProducer.send(aktivitetskort)
			log.info("Resendte siste aktivitetskort for deltaker med id $deltakerId")
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	@Unprotected
	@GetMapping("/resend/")
	fun resendSistMeldinger(
		servlet: HttpServletRequest,
		@RequestBody body: DeltakereBody,
	) {
		if (isInternal(servlet)) {
			body.deltakere.forEach { deltakerId ->
				val aktivitetskort = aktivitetskortService
					.getSisteMeldingForDeltaker(deltakerId)
					?.aktivitetskort
					?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke melding")
				aktivitetskortProducer.send(aktivitetskort)
				log.info("Resendte siste aktivitetskort for deltaker med id $deltakerId")
			}
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	data class DeltakereBody(
		val deltakere: List<UUID>,
	)

	private fun isInternal(servlet: HttpServletRequest): Boolean = servlet.remoteAddr == "127.0.0.1"
}
