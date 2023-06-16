package no.nav.amt.aktivitetskort.service

import no.nav.amt.aktivitetskort.domain.AktivitetStatus
import no.nav.amt.aktivitetskort.domain.Aktivitetskort
import no.nav.amt.aktivitetskort.domain.Arrangor
import no.nav.amt.aktivitetskort.domain.Deltaker
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.Detalj
import no.nav.amt.aktivitetskort.domain.EndretAv
import no.nav.amt.aktivitetskort.domain.Etikett
import no.nav.amt.aktivitetskort.domain.IdentType
import no.nav.amt.aktivitetskort.domain.Melding
import no.nav.amt.aktivitetskort.repositories.ArrangorRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerlisteRepository
import no.nav.amt.aktivitetskort.repositories.MeldingRepository
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.RuntimeException

@Service
class MeldingService(
	private val meldingRepository: MeldingRepository,
	private val arrangorRepository: ArrangorRepository,
	private val deltakerlisteRepository: DeltakerlisteRepository,
) {

	fun publiserEndring(melding: Melding) {
		TODO("Skriv til kafka")
	}

	fun publiserEndringer(meldinger: List<Melding>) {
		TODO("Skriv til kafka")
		TODO("Upsert meldinger")
	}

	fun publiserEndring(deltakerliste: Deltakerliste) {
		val gammleMeldinger = meldingRepository.getByDeltakerlisteId(deltakerliste.id)
		val arrangor = arrangorRepository.get(deltakerliste.arrangorId)
			?: throw RuntimeException("Kunne ikke oppdatere melding: Arrangør ${deltakerliste.arrangorId} finnes ikke")

		val oppdaterteMeldinger = gammleMeldinger.map { oppdaterTittel(it, lagTittel(deltakerliste, arrangor)) }
		//publiserEndringer(oppdaterteMeldinger))
	}

	private fun oppdaterTittel(melding: Melding, tittel: String): Melding {
		val aktivitetskort = melding.aktivitetskort.copy(tittel = tittel)
		return melding.copy(aktivitetskort = aktivitetskort)
	}

	fun publiserEndring(arrangor: Arrangor) {
		val gammleMeldinger = meldingRepository.getByArrangorId(arrangor.id)
		val oppdaterteMeldinger = gammleMeldinger.map { oppdaterArrangor(it, arrangor.navn) }
		//publiserEndringer(oppdaterteMeldinger))
	}

	private fun oppdaterArrangor(melding: Melding, arrangorNavn: String): Melding {
		val detaljer = melding.aktivitetskort.detaljer.map {
			if (it.label == "Arrangør") return@map Detalj("Arrangør", arrangorNavn)
			return@map it
		}
		return melding.copy(aktivitetskort = melding.aktivitetskort.copy(detaljer = detaljer))
	}

	fun opprettMelding(deltaker: Deltaker): Melding {
		val gammelMelding = meldingRepository.getByDeltakerId(deltaker.id)
			?: return opprettMelding(deltaker, UUID.randomUUID())
		return opprettMelding(deltaker, gammelMelding.aktivitetskort.id)
	}

	private fun opprettMelding(deltaker: Deltaker, aktivitetskortId: UUID): Melding {
		val deltakerliste = deltakerlisteRepository.get(deltaker.deltakerlisteId)
			?: throw RuntimeException("Deltakerliste ${deltaker.deltakerlisteId} finnes ikke")
		val arrangor = arrangorRepository.get(deltakerliste.arrangorId)
			?: throw RuntimeException("Arrangør ${deltakerliste.arrangorId} finnes ikke")

		val aktivitetskort = nyttAktivitetskort(aktivitetskortId, deltaker, deltakerliste, arrangor)

		return Melding(
				deltakerId = deltaker.id,
				deltakerlisteId = deltakerliste.id,
				arrangorId = arrangor.id,
				aktivitetskort = aktivitetskort,
			)
	}

	private fun nyttAktivitetskort(id: UUID, deltaker: Deltaker, deltakerliste: Deltakerliste, arrangor: Arrangor): Aktivitetskort {
		return Aktivitetskort(
			id = id,
			personident = deltaker.personident,
			tittel = lagTittel(deltakerliste, arrangor),
			aktivitetStatus = AktivitetStatus.from(deltaker.status.type),
			startDato = deltaker.oppstartsdato,
			sluttDato = deltaker.sluttdato,
			beskrivelse = null,
			endretAv = EndretAv("En ident", IdentType.SYSTEM), // TODO: Hvilken ident skal sendes med?
			endretTidspunkt = LocalDateTime.now(),
			avtaltMedNav = true,
			oppgave = null,
			handlinger = null,
			detaljer = listOfNotNull(
				deltaker.oppstartsdato?.let { Detalj("Oppstart", formaterDato(it)) },
				deltaker.sluttdato?.let { Detalj("Sluttdato", formaterDato(it)) },
				Detalj("Status for deltakelse", deltaker.status.display())
			),
			etiketter = listOfNotNull(finnEtikett(deltaker.status))
		)
	}

	private fun lagTittel(deltakerliste: Deltakerliste, arrangor: Arrangor) =
		"${deltakerliste.tiltakstype} hos ${arrangor.navn}"


	private fun finnEtikett(status: DeltakerStatus): Etikett? {
		return when (status.type) {
			DeltakerStatus.Type.VENTER_PA_OPPSTART -> Etikett(Etikett.Kode.VENTER_PA_OPPSTART)
			DeltakerStatus.Type.SOKT_INN -> Etikett(Etikett.Kode.SOKT_INN)
			DeltakerStatus.Type.VURDERES -> Etikett(Etikett.Kode.VURDERES)
			DeltakerStatus.Type.VENTELISTE -> Etikett(Etikett.Kode.VENTELISTE)
			DeltakerStatus.Type.IKKE_AKTUELL -> Etikett(Etikett.Kode.IKKE_AKTUELL)
			DeltakerStatus.Type.DELTAR -> null
			DeltakerStatus.Type.HAR_SLUTTET -> null
			DeltakerStatus.Type.FEILREGISTRERT -> null
			DeltakerStatus.Type.AVBRUTT -> null
			DeltakerStatus.Type.PABEGYNT_REGISTRERING -> null
		}
	}

	private fun formaterDato(oppstartsdato: LocalDate): String {
		return oppstartsdato.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
	}

	fun upsert(melding: Melding): RepositoryResult<Melding> {
		return meldingRepository.upsert(melding)
	}

}
