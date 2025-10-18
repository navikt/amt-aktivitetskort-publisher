package no.nav.amt.aktivitetskort.database

import no.nav.amt.aktivitetskort.domain.AktivitetStatus
import no.nav.amt.aktivitetskort.domain.Aktivitetskort
import no.nav.amt.aktivitetskort.domain.Arrangor
import no.nav.amt.aktivitetskort.domain.Deltaker
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.domain.Detalj
import no.nav.amt.aktivitetskort.domain.EndretAv
import no.nav.amt.aktivitetskort.domain.Handling
import no.nav.amt.aktivitetskort.domain.IdentType
import no.nav.amt.aktivitetskort.domain.Kilde
import no.nav.amt.aktivitetskort.domain.LenkeType
import no.nav.amt.aktivitetskort.domain.Melding
import no.nav.amt.aktivitetskort.domain.Oppfolgingsperiode
import no.nav.amt.aktivitetskort.domain.Tag
import no.nav.amt.aktivitetskort.domain.Tiltak
import no.nav.amt.aktivitetskort.domain.Tiltakstype
import no.nav.amt.aktivitetskort.kafka.consumer.dto.ArrangorDto
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerDto
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerlistePayload
import no.nav.amt.aktivitetskort.service.StatusMapping.deltakerStatusTilAktivitetStatus
import no.nav.amt.aktivitetskort.service.StatusMapping.deltakerStatusTilEtikett
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

object TestData {
	const val VEILEDER_URL_BASEPATH = "https://intern.veileder"
	const val DELTAKER_URL_BASEPATH = "https://ekstern.deltaker"

	fun melding(
		deltakerId: UUID = UUID.randomUUID(),
		deltakerlisteId: UUID = UUID.randomUUID(),
		arrangorId: UUID = UUID.randomUUID(),
		aktivitetskort: Aktivitetskort = aktivitetskort(),
		oppfolgingsperiode: UUID? = null,
	) = Melding(aktivitetskort.id, deltakerId, deltakerlisteId, arrangorId, aktivitetskort, oppfolgingsperiode)

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
		etiketter: List<Tag> = listOf(),
		tiltakstype: Tiltakskode = Tiltakskode.OPPFOLGING,
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
		tiltakstype = tiltakstype.toArenaKode(),
	)

	fun aktivitetskort(
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
		avtaltMedNav = true,
		oppgave = null,
		handlinger = listOf(
			Handling(
				tekst = "Les mer om din deltakelse",
				subtekst = "",
				url = "$VEILEDER_URL_BASEPATH/${deltaker.id}",
				lenkeType = LenkeType.INTERN,
			),
			Handling(
				tekst = "Les mer om din deltakelse",
				subtekst = "",
				url = "$DELTAKER_URL_BASEPATH/${deltaker.id}",
				lenkeType = LenkeType.EKSTERN,
			),
		),
		detaljer = listOfNotNull(
			Detalj("Status for deltakelse", deltaker.status.display()),
			Detalj("Arrangør", arrangor.navn),
		),
		etiketter = listOfNotNull(deltakerStatusTilEtikett(deltaker.status)),
		tiltakstype = deltakerliste.tiltak.tiltakskode.toArenaKode(),
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
		kilde: Kilde = Kilde.ARENA,
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
		kilde,
	)

	fun oppfolgingsperiode(
		id: UUID = UUID.randomUUID(),
		startDato: LocalDateTime = LocalDateTime.now().minusWeeks(4),
		sluttdato: LocalDateTime? = null,
	) = Oppfolgingsperiode(id, startDato = startDato, sluttDato = sluttdato)

	fun arrangor(
		id: UUID = UUID.randomUUID(),
		organisasjonsnummer: String = (100_000_000..900_000_000).random().toString(),
		navn: String = "Navn",
		overordnetArrangorId: UUID? = null,
	) = Arrangor(id, organisasjonsnummer, navn, overordnetArrangorId)

	fun deltakerliste(
		id: UUID = UUID.randomUUID(),
		tiltak: Tiltak = Tiltak("Oppfølging", Tiltakskode.OPPFOLGING),
		navn: String = "navn",
		arrangorId: UUID = UUID.randomUUID(),
	) = Deltakerliste(id, tiltak, navn, arrangorId)

	data class MockContext(
		val deltaker: Deltaker = deltaker(),
		val deltakerliste: Deltakerliste = deltakerliste(id = deltaker.deltakerlisteId),
		val arrangor: Arrangor = arrangor(id = deltakerliste.arrangorId),
		val tiltakstype: Tiltakstype = Tiltakstype(
			id = UUID.randomUUID(),
			navn = "Oppfølging",
			tiltakskode = Tiltakskode.OPPFOLGING,
		),
		val aktivitetskortId: UUID = UUID.randomUUID(),
		val aktivitetskort: Aktivitetskort = aktivitetskort(aktivitetskortId, deltaker, deltakerliste, arrangor),
		val oppfolgingsperiodeId: UUID? = null,
		val melding: Melding = melding(deltaker.id, deltakerliste.id, arrangor.id, aktivitetskort, oppfolgingsperiodeId),
	) {
		fun deltakerlistePayload() = DeltakerlistePayload(
			id = this.deltakerliste.id,
			tiltakstype = this.deltakerliste.tiltak.toDto(),
			navn = this.deltakerliste.navn,
			virksomhetsnummer = this@MockContext.arrangor.organisasjonsnummer,
		)
	}

	fun Deltakerliste.toDto(arrangor: Arrangor) = DeltakerlistePayload(
		id = this.id,
		tiltakstype = this.tiltak.toDto(),
		navn = this.navn,
		virksomhetsnummer = arrangor.organisasjonsnummer,
	)

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
		kilde = this.kilde,
	)

	fun Arrangor.toDto() = ArrangorDto(
		id = this.id,
		organisasjonsnummer = this.organisasjonsnummer,
		navn = this.navn,
		overordnetArrangorId = this.overordnetArrangorId,
	)

	fun Tiltak.toDto(): DeltakerlistePayload.Tiltakstype = DeltakerlistePayload.Tiltakstype(this.tiltakskode.name)
}
