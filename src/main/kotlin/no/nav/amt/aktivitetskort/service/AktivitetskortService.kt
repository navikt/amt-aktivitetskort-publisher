package no.nav.amt.aktivitetskort.service

import no.nav.amt.aktivitetskort.domain.Aktivitetskort
import no.nav.amt.aktivitetskort.domain.Arrangor
import no.nav.amt.aktivitetskort.domain.Deltaker
import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.Detalj
import no.nav.amt.aktivitetskort.domain.EndretAv
import no.nav.amt.aktivitetskort.domain.IdentType
import no.nav.amt.aktivitetskort.domain.Melding
import no.nav.amt.aktivitetskort.repositories.ArrangorRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerlisteRepository
import no.nav.amt.aktivitetskort.repositories.MeldingRepository
import no.nav.amt.aktivitetskort.service.StatusMapping.deltakerStatusTilAktivetStatus
import no.nav.amt.aktivitetskort.service.StatusMapping.deltakerStatusTilEtikett
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class AktivitetskortService(
	private val meldingRepository: MeldingRepository,
	private val arrangorRepository: ArrangorRepository,
	private val deltakerlisteRepository: DeltakerlisteRepository,
	private val deltakerRepository: DeltakerRepository,
) {

	fun lagAktivitetskort(deltaker: Deltaker): Aktivitetskort {
		val melding = meldingRepository
			.getByDeltakerId(deltaker.id)
			?.let { opprettMelding(deltaker, it.aktivitetskort.id) }
			?: opprettMelding(deltaker, UUID.randomUUID())

		return melding.aktivitetskort
	}

	fun lagAktivitetskort(deltakerliste: Deltakerliste) = meldingRepository
		.getByDeltakerlisteId(deltakerliste.id)
		.map { opprettMelding(it.deltakerId, it.aktivitetskort.id).aktivitetskort }

	fun lagAktivitetskort(arrangor: Arrangor) = meldingRepository
		.getByArrangorId(arrangor.id)
		.map { opprettMelding(it.deltakerId, it.aktivitetskort.id).aktivitetskort }

	private fun opprettMelding(deltakerId: UUID, aktivitetskortId: UUID) = deltakerRepository.get(deltakerId)
		?.let { opprettMelding(it, aktivitetskortId) }
		?: throw RuntimeException("Deltaker $deltakerId finnes ikke")

	private fun opprettMelding(deltaker: Deltaker, aktivitetskortId: UUID): Melding {
		val deltakerliste = deltakerlisteRepository.get(deltaker.deltakerlisteId)
			?: throw RuntimeException("Deltakerliste ${deltaker.deltakerlisteId} finnes ikke")
		val arrangor = arrangorRepository.get(deltakerliste.arrangorId)
			?: throw RuntimeException("Arrangør ${deltakerliste.arrangorId} finnes ikke")

		val aktivitetskort = nyttAktivitetskort(aktivitetskortId, deltaker, deltakerliste, arrangor)

		val melding = Melding(
			deltakerId = deltaker.id,
			deltakerlisteId = deltakerliste.id,
			arrangorId = arrangor.id,
			aktivitetskort = aktivitetskort,
		)

		meldingRepository.upsert(melding)

		return melding
	}

	private fun nyttAktivitetskort(id: UUID, deltaker: Deltaker, deltakerliste: Deltakerliste, arrangor: Arrangor) = Aktivitetskort(
		id = id,
		personident = deltaker.personident,
		tittel = Aktivitetskort.lagTittel(deltakerliste.tiltaksnavn, arrangor.navn),
		aktivitetStatus = deltakerStatusTilAktivetStatus(deltaker.status.type),
		startDato = deltaker.oppstartsdato,
		sluttDato = deltaker.sluttdato,
		beskrivelse = null,
		endretAv = EndretAv("amt-aktivitetskort-publisher", IdentType.SYSTEM),
		endretTidspunkt = LocalDateTime.now(),
		avtaltMedNav = true,
		oppgave = null,
		handlinger = null,
		detaljer = listOfNotNull(
			Detalj("Status for deltakelse", deltaker.status.display()),
			Detalj("Arrangør", arrangor.navn),
		),
		etiketter = listOfNotNull(deltakerStatusTilEtikett(deltaker.status)),
	)
}
