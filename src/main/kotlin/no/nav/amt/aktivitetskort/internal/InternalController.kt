package no.nav.amt.aktivitetskort.internal

import jakarta.servlet.http.HttpServletRequest
import no.nav.amt.aktivitetskort.client.AktivitetArenaAclClient
import no.nav.amt.aktivitetskort.client.AmtArenaAclClient
import no.nav.amt.aktivitetskort.kafka.producer.AktivitetskortProducer
import no.nav.amt.aktivitetskort.service.AktivitetskortService
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/internal")
class InternalController(
	private val aktivitetskortService: AktivitetskortService,
	private val aktivitetskortProducer: AktivitetskortProducer,
	private val arenaAclClient: AmtArenaAclClient,
	private val aktivitetArenaAclClient: AktivitetArenaAclClient,
) {
	private val log = LoggerFactory.getLogger(InternalController::class.java)

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
			val aktivitetskort = aktivitetskortService.getMelding(
				deltakerId,
			)?.aktivitetskort ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke melding")
			aktivitetskortProducer.send(aktivitetskort)
			log.info("Resendte siste aktivitetskort for deltaker med id $deltakerId")
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	@Unprotected
	@PostMapping("/reprosesser-med-ny-id")
	fun reprosesserMedNyId(
		servlet: HttpServletRequest,
		@RequestBody body: DeltakereToReprocess,
	) {
		if (isInternal(servlet)) {
			body.meldingIder.forEach { meldingId ->
				val meldingerMedDuplisertId = aktivitetskortService.getMeldingerById(UUID.fromString(meldingId))
				log.info("Fant ${meldingerMedDuplisertId.size} meldinger med duplisert id $meldingId")

				meldingerMedDuplisertId.forEach { melding ->
					log.info("Behandler melding ${melding.id} med deltakerid ${melding.deltakerId}")

					arenaAclClient.getArenaIdForAmtId(melding.deltakerId)
						?.also {
							log.info(
								"Deltaker ${melding.deltakerId} med meldingId ${melding.id} finnes i arena-acl med id $it og skal få samme id på nytt",
							)
						}
						?.let { aktivitetArenaAclClient.getAktivitetIdForArenaId(it) }
						.also {
							if (it == null) {
								val nyId = UUID.randomUUID()
								log.info("Deltaker ${melding.deltakerId} fikk ikke id fra dab. Genererer ny $nyId")

								if (body.skalGenerereNyeMeldinger) {
									aktivitetskortService.opprettMelding(deltakerId = melding.deltakerId, meldingId = nyId)
									log.info("Ny melding for deltaker ${melding.deltakerId} opprettet med id $nyId")
								}
							} else if (it != melding.id) {
								log.info("Deltaker ${melding.deltakerId} med tidligere meldingId ${melding.id} fikk ny meldingid $it fra dab")
							} else {
								log.info("Deltaker ${melding.deltakerId} med tidligere meldingId ${melding.id} fikk beholde meldingsId")
							}
						}
				}
			}
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	data class DeltakereToReprocess(
		val meldingIder: List<String>,
		val skalGenerereNyeMeldinger: Boolean,
	)

	private fun isInternal(servlet: HttpServletRequest): Boolean {
		return servlet.remoteAddr == "127.0.0.1"
	}
}
