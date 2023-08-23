package no.nav.amt.aktivitetskort.database

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
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.aktivitetskort.kafka.consumer.dto.ArrangorDto
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerDto
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerlisteDto
import no.nav.amt.aktivitetskort.service.StatusMapping.deltakerStatusTilAktivetStatus
import no.nav.amt.aktivitetskort.service.StatusMapping.deltakerStatusTilEtikett
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

object TestData {
	fun melding(
		deltakerId: UUID = UUID.randomUUID(),
		deltakerlisteId: UUID = UUID.randomUUID(),
		arrangorId: UUID = UUID.randomUUID(),
		melding: Aktivitetskort = aktivitetskort(),
	) = Melding(deltakerId, deltakerlisteId, arrangorId, melding)

	fun aktivitetskort(
		id: UUID = UUID.randomUUID(),
		personIdent: String = UUID.randomUUID().toString(),
		tittel: String = UUID.randomUUID().toString(),
		aktivitetStatus: AktivitetStatus = AktivitetStatus.GJENNOMFORES,
		startDato: LocalDate = LocalDate.now().minusDays(7),
		sluttDato: LocalDate = LocalDate.now().plusDays(7),
		beskrivelse: String? = null,
		endretAv: EndretAv = EndretAv(UUID.randomUUID().toString(), IdentType.SYSTEM),
		endretAvTidspunkt: LocalDateTime = LocalDateTime.now(),
		avtaltMedNav: Boolean = true,
		detaljer: List<Detalj> = listOf(Detalj("Label", "Verdi")),
		etiketter: List<Etikett> = listOf(),
	) = Aktivitetskort(
		id = id,
		personident = personIdent,
		tittel = tittel,
		aktivitetStatus = aktivitetStatus,
		startDato = startDato,
		sluttDato = sluttDato,
		beskrivelse = beskrivelse,
		endretAv = endretAv,
		endretTidspunkt = endretAvTidspunkt,
		avtaltMedNav = avtaltMedNav,
		oppgave = null,
		handlinger = null,
		detaljer = detaljer,
		etiketter = etiketter,
	)

	fun aktivitetskort(id: UUID, deltaker: Deltaker, deltakerliste: Deltakerliste, arrangor: Arrangor) =
		Aktivitetskort(
			id = id,
			personident = deltaker.personident,
			tittel = Aktivitetskort.lagTittel(deltakerliste, arrangor),
			aktivitetStatus = deltakerStatusTilAktivetStatus(deltaker.status.type).getOrThrow(),
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

	fun deltaker(
		id: UUID = UUID.randomUUID(),
		personident: String = "fnr",
		deltakerlisteId: UUID = UUID.randomUUID(),
		status: DeltakerStatus = DeltakerStatus(DeltakerStatus.Type.DELTAR, null),
		dagerPerUke: Float? = 5.0f,
		prosentStilling: Double? = 100.0,
		oppstartsdato: LocalDate? = LocalDate.now().minusWeeks(4),
		sluttdato: LocalDate? = LocalDate.now().plusWeeks(4),
		deltarPaKurs: Boolean = false,
	) = Deltaker(
		id,
		personident,
		deltakerlisteId,
		status,
		dagerPerUke,
		prosentStilling,
		oppstartsdato,
		sluttdato,
		deltarPaKurs,
	)

	fun arrangor(
		id: UUID = UUID.randomUUID(),
		organisasjonsnummer: String = (100_000_000..900_000_000).random().toString(),
		navn: String = "navn",
	) = Arrangor(id, organisasjonsnummer, navn)

	fun deltakerliste(
		id: UUID = UUID.randomUUID(),
		tiltak: Tiltak = Tiltak("Oppfølging", Tiltak.Type.OPPFOELGING),
		navn: String = "navn",
		arrangorId: UUID = UUID.randomUUID(),
	) = Deltakerliste(id, tiltak, navn, arrangorId)

	data class MockContext(
		val deltaker: Deltaker = deltaker(),
		val deltakerliste: Deltakerliste = deltakerliste(id = deltaker.deltakerlisteId),
		val arrangor: Arrangor = arrangor(id = deltakerliste.arrangorId),
		val aktivitetskortId: UUID = UUID.randomUUID(),
		val aktivitetskort: Aktivitetskort = aktivitetskort(aktivitetskortId, deltaker, deltakerliste, arrangor),
		val melding: Melding = melding(deltaker.id, deltakerliste.id, arrangor.id, aktivitetskort),
	) {
		fun deltakerlisteDto() = DeltakerlisteDto(
			id = this.deltakerliste.id,
			tiltakstype = this.deltakerliste.tiltak.toDto(),
			navn = this.deltakerliste.navn,
			virksomhetsnummer = this@MockContext.arrangor.organisasjonsnummer,
		)
	}

	fun Deltaker.toDto() = DeltakerDto(
		id = this.id,
		personalia = DeltakerDto.DeltakerPersonaliaDto(this.personident),
		deltakerlisteId = this.deltakerlisteId,
		status = DeltakerDto.DeltakerStatusDto(this.status.type, this.status.aarsak),
		dagerPerUke = this.dagerPerUke,
		prosentStilling = this.prosentStilling,
		oppstartsdato = this.oppstartsdato,
		sluttdato = this.sluttdato,
		deltarPaKurs = this.deltarPaKurs,
	)

	fun Arrangor.toDto() = ArrangorDto(
		id = this.id,
		organisasjonsnummer = this.organisasjonsnummer,
		navn = this.navn,
	)

	fun Tiltak.toDto(): DeltakerlisteDto.Tiltakstype {
		val type = when (this.type) {
			Tiltak.Type.OPPFOELGING -> "INDOPPFAG"
			Tiltak.Type.VARIG_TILRETTELAGT_ARBEID -> "VASV"
			Tiltak.Type.AVKLARING -> "AVKLARAG"
			Tiltak.Type.ARBEIDSFORBEREDENDE_TRENING -> "ARBFORB"
			Tiltak.Type.ARBEIDSRETTET_REHABILITERING -> "ARBRRHDAG"
			Tiltak.Type.ARBEIDSMARKEDSOPPLAERING -> "GRUPPEAMO"
			Tiltak.Type.DIGITALT_OPPFOELGINGSTILTAK -> "DIGIOPPARB"
			Tiltak.Type.JOBBKLUBB -> "JOBBK"
			else -> "ANNETTILTAK"
		}

		val navn = when (this.navn) {
			"Arbeidsforberedende trening" -> "Arbeidsforberedende trening (AFT)"
			"Varig tilrettelagt arbeid" -> "Varig tilrettelagt arbeid i skjermet virksomhet"
			"Arbeidsmarkedsopplæring" -> "Gruppe AMO"
			"Arbeidsrettet rehabilitering" -> "Arbeidsrettet rehabilitering (dag)"
			"Digitalt oppfølgingstiltak" -> "Digitalt oppfølgingstiltak for arbeidsledige (jobbklubb)"
			else -> this.navn
		}
		return DeltakerlisteDto.Tiltakstype(navn, type)
	}
}
