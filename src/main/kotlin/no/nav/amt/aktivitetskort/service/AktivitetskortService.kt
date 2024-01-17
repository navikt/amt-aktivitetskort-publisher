package no.nav.amt.aktivitetskort.service

import io.getunleash.Unleash
import no.nav.amt.aktivitetskort.client.AktivitetArenaAclClient
import no.nav.amt.aktivitetskort.client.AmtArenaAclClient
import no.nav.amt.aktivitetskort.domain.Aktivitetskort
import no.nav.amt.aktivitetskort.domain.Arrangor
import no.nav.amt.aktivitetskort.domain.Deltaker
import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.EndretAv
import no.nav.amt.aktivitetskort.domain.IdentType
import no.nav.amt.aktivitetskort.domain.Melding
import no.nav.amt.aktivitetskort.repositories.ArrangorRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerlisteRepository
import no.nav.amt.aktivitetskort.repositories.MeldingRepository
import no.nav.amt.aktivitetskort.service.StatusMapping.deltakerStatusTilAktivitetStatus
import no.nav.amt.aktivitetskort.service.StatusMapping.deltakerStatusTilEtikett
import no.nav.amt.aktivitetskort.utils.EnvUtils.isDev
import org.slf4j.LoggerFactory
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
	private val unleash: Unleash,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	fun getMelding(deltakerId: UUID) = meldingRepository.getByDeltakerId(deltakerId)

	fun lagAktivitetskort(deltaker: Deltaker): Aktivitetskort {
		val melding = opprettMelding(deltaker)

		log.info("Opprettet nytt aktivitetskort: ${melding.aktivitetskort.id} for deltaker: ${deltaker.id}")
		return melding.aktivitetskort
	}

	fun lagAktivitetskort(deltakerId: UUID): Aktivitetskort {
		val melding = opprettMelding(deltakerId)

		log.info("Opprettet nytt aktivitetskort: ${melding.aktivitetskort.id} for deltaker: $deltakerId")
		return melding.aktivitetskort
	}

	fun lagAktivitetskort(deltakerliste: Deltakerliste) = meldingRepository
		.getByDeltakerlisteId(deltakerliste.id)
		.map { opprettMelding(it.deltakerId).aktivitetskort }
		.also { log.info("Opprettet nye aktivitetskort for deltakerliste: ${deltakerliste.id}") }

	fun lagAktivitetskort(arrangor: Arrangor): List<Aktivitetskort> {
		val underordnedeArrangorer = arrangorRepository.getUnderordnedeArrangorer(arrangor.id)
		val alleOppdaterteArrangorer = listOf(arrangor) + underordnedeArrangorer
		return alleOppdaterteArrangorer.flatMap { a ->
			meldingRepository
				.getByArrangorId(a.id)
				.filter { it.aktivitetskort.erAktivDeltaker() }
				.map { opprettMelding(it.deltakerId).aktivitetskort }
				.also { log.info("Opprettet nye aktivitetskort for arrangør: ${a.id}") }
		}
	}

	private fun getAktivitetskortId(deltakerId: UUID): UUID {
		val aktivitetskortId = amtArenaAclClient.getArenaIdForAmtId(deltakerId)
			?.let { aktivitetArenaAclClient.getAktivitetIdForArenaId(it) }

		if (aktivitetskortId != null) {
			return aktivitetskortId
		} else if (unleash.isEnabled("amt.enable-komet-deltakere") && isDev()) {
			log.info("Definerer egen aktivitetskortId for deltaker med id $deltakerId")
			return UUID.randomUUID()
		} else {
			throw IllegalStateException("Kunne ikke hente aktivitetskortId for deltaker med id $deltakerId")
		}
	}

	private fun opprettMelding(deltakerId: UUID) = deltakerRepository.get(deltakerId)
		?.let { opprettMelding(it) }
		?: throw RuntimeException("Deltaker $deltakerId finnes ikke")

	private fun opprettMelding(deltaker: Deltaker): Melding {
		val deltakerliste = deltakerlisteRepository.get(deltaker.deltakerlisteId)
			?: throw RuntimeException("Deltakerliste ${deltaker.deltakerlisteId} finnes ikke")
		val arrangor = arrangorRepository.get(deltakerliste.arrangorId)
			?: throw RuntimeException("Arrangør ${deltakerliste.arrangorId} finnes ikke")
		val overordnetArrangor = arrangor.overordnetArrangorId?.let { arrangorRepository.get(it) }
		val aktivitetskortId = getAktivitetskortId(deltaker.id)

		val aktivitetskort = overordnetArrangor?.let { nyttAktivitetskort(aktivitetskortId, deltaker, deltakerliste, it) }
			?: nyttAktivitetskort(aktivitetskortId, deltaker, deltakerliste, arrangor)

		val melding = Melding(
			deltakerId = deltaker.id,
			deltakerlisteId = deltakerliste.id,
			arrangorId = arrangor.id,
			aktivitetskort = aktivitetskort,
		)

		meldingRepository.upsert(melding)

		return melding
	}

	private fun nyttAktivitetskort(id: UUID, deltaker: Deltaker, deltakerliste: Deltakerliste, arrangor: Arrangor) =
		Aktivitetskort(
			id = id,
			personident = deltaker.personident,
			tittel = Aktivitetskort.lagTittel(deltakerliste, arrangor, deltaker.deltarPaKurs),
			aktivitetStatus = deltakerStatusTilAktivitetStatus(deltaker.status.type).getOrThrow(),
			startDato = deltaker.oppstartsdato,
			sluttDato = deltaker.sluttdato,
			beskrivelse = null,
			endretAv = EndretAv("amt-aktivitetskort-publisher", IdentType.SYSTEM),
			endretTidspunkt = LocalDateTime.now(),
			avtaltMedNav = true,
			oppgave = null,
			handlinger = null,
			detaljer = Aktivitetskort.lagDetaljer(deltaker, deltakerliste, arrangor),
			etiketter = listOfNotNull(deltakerStatusTilEtikett(deltaker.status)),
			tiltakstype = deltakerliste.tiltak.type,
		)
}
