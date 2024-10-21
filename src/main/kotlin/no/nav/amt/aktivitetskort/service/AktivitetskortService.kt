package no.nav.amt.aktivitetskort.service

import no.nav.amt.aktivitetskort.client.AktivitetArenaAclClient
import no.nav.amt.aktivitetskort.client.AmtArenaAclClient
import no.nav.amt.aktivitetskort.domain.Aktivitetskort
import no.nav.amt.aktivitetskort.domain.Arrangor
import no.nav.amt.aktivitetskort.domain.Deltaker
import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.EndretAv
import no.nav.amt.aktivitetskort.domain.Handling
import no.nav.amt.aktivitetskort.domain.IKKE_AVTALT_MED_NAV_STATUSER
import no.nav.amt.aktivitetskort.domain.IdentType
import no.nav.amt.aktivitetskort.domain.LenkeType
import no.nav.amt.aktivitetskort.domain.Melding
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.aktivitetskort.repositories.ArrangorRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerlisteRepository
import no.nav.amt.aktivitetskort.repositories.MeldingRepository
import no.nav.amt.aktivitetskort.service.StatusMapping.deltakerStatusTilAktivitetStatus
import no.nav.amt.aktivitetskort.service.StatusMapping.deltakerStatusTilEtikett
import no.nav.amt.aktivitetskort.unleash.UnleashToggle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class AktivitetskortService(
	private val meldingRepository: MeldingRepository,
	private val arrangorRepository: ArrangorRepository,
	private val deltakerlisteRepository: DeltakerlisteRepository,
	private val deltakerRepository: DeltakerRepository,
	private val aktivitetArenaAclClient: AktivitetArenaAclClient,
	private val amtArenaAclClient: AmtArenaAclClient,
	private val unleashToggle: UnleashToggle,
	@Value("\${veilederurl.basepath}") private val veilederUrlBasePath: String,
	@Value("\${deltakerurl.basepath}") private val deltakerUrlBasePath: String,
) {
	private val log = LoggerFactory.getLogger(javaClass)

	fun getMelding(deltakerId: UUID) = meldingRepository.getByDeltakerId(deltakerId)

	fun lagAktivitetskort(deltaker: Deltaker): Aktivitetskort {
		val melding = opprettMelding(deltaker)

		log.info("Opprettet nytt aktivitetskort: ${melding.aktivitetskort.id} for deltaker: ${deltaker.id}")
		return melding.aktivitetskort
	}

	fun lagAktivitetskort(deltakerId: UUID): Aktivitetskort {
		val melding = opprettMelding(deltakerId) ?: throw RuntimeException("Deltaker $deltakerId finnes ikke")

		log.info("Opprettet nytt aktivitetskort: ${melding.aktivitetskort.id} for deltaker: $deltakerId")
		return melding.aktivitetskort
	}

	fun lagAktivitetskort(deltakerliste: Deltakerliste) = meldingRepository
		.getByDeltakerlisteId(deltakerliste.id)
		.mapNotNull { opprettMelding(it.deltakerId)?.aktivitetskort }
		.also { log.info("Opprettet nye aktivitetskort for deltakerliste: ${deltakerliste.id}") }

	fun lagAktivitetskort(arrangor: Arrangor): List<Aktivitetskort> {
		val underordnedeArrangorer = arrangorRepository.getUnderordnedeArrangorer(arrangor.id)
		val alleOppdaterteArrangorer = listOf(arrangor) + underordnedeArrangorer
		return alleOppdaterteArrangorer.flatMap { a ->
			meldingRepository
				.getByArrangorId(a.id)
				.filter { it.aktivitetskort.erAktivDeltaker() }
				.mapNotNull { opprettMelding(it.deltakerId)?.aktivitetskort }
				.also { log.info("Opprettet nye aktivitetskort for arrangør: ${a.id}") }
		}
	}

	private fun getAktivitetskortId(deltakerId: UUID, tiltakstype: Tiltak.Type): UUID {
		if (deltakerId == UUID.fromString("ac119d61-8370-4020-8ef8-d9d3ac6a4ad4") && aktivitetskortIdForDeltaker(deltakerId) == null) {
			val nyId = UUID.randomUUID()
			log.warn("Setter ny id for hist-deltaker med id $deltakerId: $nyId")
			return nyId
		}

		val eksisterendeAktivitetskortId = aktivitetskortIdForDeltaker(deltakerId)
			?: amtArenaAclClient.getArenaIdForAmtId(deltakerId)
				?.let { aktivitetArenaAclClient.getAktivitetIdForArenaId(it) }

		if (unleashToggle.erKometMasterForTiltakstype(tiltakstype)) {
			return eksisterendeAktivitetskortId
				?: UUID.randomUUID().also { log.info("Definerer egen aktivitetskortId: $it for deltaker med id $deltakerId") }
		}

		return eksisterendeAktivitetskortId
			?: throw IllegalStateException("Kunne ikke hente aktivitetskortId for deltaker med id $deltakerId")
	}

	private fun aktivitetskortIdForDeltaker(deltakerId: UUID) = meldingRepository.getByDeltakerId(deltakerId)?.aktivitetskort?.id

	private fun opprettMelding(deltakerId: UUID): Melding? {
		val deltaker = deltakerRepository.get(deltakerId)
		if (deltaker == null) {
			log.warn("Deltaker med id $deltakerId finnes ikke lenger")
			return null
		}
		return opprettMelding(deltaker)
	}

	private fun opprettMelding(deltaker: Deltaker): Melding {
		val deltakerliste = deltakerlisteRepository.get(deltaker.deltakerlisteId)
			?: throw RuntimeException("Deltakerliste ${deltaker.deltakerlisteId} finnes ikke")
		val arrangor = arrangorRepository.get(deltakerliste.arrangorId)
			?: throw RuntimeException("Arrangør ${deltakerliste.arrangorId} finnes ikke")
		val overordnetArrangor = arrangor.overordnetArrangorId?.let { arrangorRepository.get(it) }
		val aktivitetskortId = getAktivitetskortId(deltaker.id, deltakerliste.tiltak.type)

		val aktivitetskort = nyttAktivitetskort(
			aktivitetskortId,
			deltaker,
			deltakerliste,
			getArrangorForAktivitetskort(arrangor = arrangor, overordnetArrangor = overordnetArrangor),
		)

		val melding = Melding(
			deltakerId = deltaker.id,
			deltakerlisteId = deltakerliste.id,
			arrangorId = arrangor.id,
			aktivitetskort = aktivitetskort,
		)

		meldingRepository.upsert(melding)

		return melding
	}

	private fun getArrangorForAktivitetskort(arrangor: Arrangor, overordnetArrangor: Arrangor?): Arrangor {
		return if (overordnetArrangor == null) {
			arrangor
		} else {
			if (overordnetArrangor.navn == "Ukjent Virksomhet") {
				arrangor
			} else {
				overordnetArrangor
			}
		}
	}

	private fun nyttAktivitetskort(
		id: UUID,
		deltaker: Deltaker,
		deltakerliste: Deltakerliste,
		arrangor: Arrangor,
	) = Aktivitetskort(
		id = id,
		personident = deltaker.personident,
		tittel = Aktivitetskort.lagTittel(deltakerliste, arrangor, deltaker.deltarPaKurs),
		aktivitetStatus = deltakerStatusTilAktivitetStatus(deltaker.status.type).getOrThrow(),
		startDato = deltaker.oppstartsdato,
		sluttDato = deltaker.sluttdato,
		beskrivelse = null,
		endretAv = EndretAv("amt-aktivitetskort-publisher", IdentType.SYSTEM),
		endretTidspunkt = LocalDateTime.now(),
		avtaltMedNav = deltaker.status.type !in IKKE_AVTALT_MED_NAV_STATUSER,
		oppgave = null,
		handlinger = getHandlinger(deltaker, deltakerliste.tiltak.type),
		detaljer = Aktivitetskort.lagDetaljer(deltaker, deltakerliste, arrangor),
		etiketter = listOfNotNull(deltakerStatusTilEtikett(deltaker.status)),
		tiltakstype = deltakerliste.tiltak.type,
	)

	private fun getHandlinger(deltaker: Deltaker, tiltakstype: Tiltak.Type): List<Handling>? {
		if (!unleashToggle.erKometMasterForTiltakstype(tiltakstype)) {
			return null
		}
		return listOf(
			Handling(
				tekst = "Les mer om din deltakelse",
				subtekst = "",
				url = "$veilederUrlBasePath/${deltaker.id}",
				lenkeType = LenkeType.INTERN,
			),
			Handling(
				tekst = "Les mer om din deltakelse",
				subtekst = "",
				url = "$deltakerUrlBasePath/${deltaker.id}",
				lenkeType = LenkeType.EKSTERN,
			),
		)
	}
}
