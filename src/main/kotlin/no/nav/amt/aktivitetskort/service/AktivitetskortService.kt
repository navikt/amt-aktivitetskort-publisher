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
import kotlin.RuntimeException

@Service
class AktivitetskortService(
	private val meldingRepository: MeldingRepository,
	private val arrangorRepository: ArrangorRepository,
	private val deltakerlisteRepository: DeltakerlisteRepository,
	private val deltakerRepository: DeltakerRepository,
) {

	fun lagAktivitetskort(deltaker: Deltaker): List<Aktivitetskort> {
		val eksisterendeMelding = meldingRepository.getByDeltakerId(deltaker.id)

		val melding = if (eksisterendeMelding == null) {
			opprettMelding(deltaker, UUID.randomUUID())
		} else {
			opprettMelding(deltaker, eksisterendeMelding.aktivitetskort.id)
		}

		return listOf(melding.aktivitetskort)
	}

	fun lagAktivitetskort(deltakerliste: Deltakerliste): List<Aktivitetskort> {
		val eksisterendeMeldinger = meldingRepository.getByDeltakerlisteId(deltakerliste.id)

		return eksisterendeMeldinger.map { opprettMelding(it.deltakerId, it.aktivitetskort.id).aktivitetskort }
	}

	fun lagAktivitetskort(arrangor: Arrangor): List<Aktivitetskort> {
		val eksisterendeMeldinger = meldingRepository.getByArrangorId(arrangor.id)

		return eksisterendeMeldinger.map { opprettMelding(it.deltakerId, it.aktivitetskort.id).aktivitetskort }
	}

	private fun opprettMelding(deltakerId: UUID, aktivitetskortId: UUID): Melding {
		val deltaker = deltakerRepository.get(deltakerId)
			?: throw RuntimeException("Deltaker $deltakerId finnes ikke")
		return opprettMelding(deltaker, aktivitetskortId)
	}

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

	private fun nyttAktivitetskort(id: UUID, deltaker: Deltaker, deltakerliste: Deltakerliste, arrangor: Arrangor): Aktivitetskort {
		return Aktivitetskort(
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
}
