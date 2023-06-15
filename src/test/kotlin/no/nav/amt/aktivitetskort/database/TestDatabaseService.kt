package no.nav.amt.aktivitetskort.database

import no.nav.amt.aktivitetskort.domain.AktivitetStatus
import no.nav.amt.aktivitetskort.domain.Aktivitetskort
import no.nav.amt.aktivitetskort.domain.Arrangor
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
import java.util.UUID
import javax.sql.DataSource

@Service
class TestDatabaseService(
	val meldingRepository: MeldingRepository,
	val arrangorRepository: ArrangorRepository,
	val deltakerlisteRepository: DeltakerlisteRepository,
	private val datasource: DataSource
) {

	fun clean() = DbTestDataUtils.cleanDatabase(datasource)

	fun melding(
		deltakerId: UUID = UUID.randomUUID(),
		deltakerlisteId: UUID = insertDeltakerliste(deltakerliste()).id,
		arrangorId: UUID = insertArrangor(arrangor()).id,
		melding: Aktivitetskort = aktivitetskort()
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
		etiketter: List<Etikett> = listOf()
	) = Aktivitetskort(
		id = id,
		personIdent = personIdent,
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
		etiketter = etiketter
	)

	fun arrangor(
		id: UUID = UUID.randomUUID(),
		navn: String = "navn"
	) = Arrangor(id, navn)

	fun deltakerliste(
		id: UUID = UUID.randomUUID(),
		tiltakstype: String = "tiltakstype",
		navn: String = "navn"
	) = Deltakerliste(id, tiltakstype, navn)

	private fun insertArrangor(arrangor: Arrangor = arrangor()) =
		when (val result = arrangorRepository.upsert(arrangor)) {
			is RepositoryResult.Created -> result.data
			is RepositoryResult.Modified -> result.data
			is RepositoryResult.NoChange -> arrangor
		}

	private fun insertDeltakerliste(deltakerliste: Deltakerliste = deltakerliste()) =
		when (val result = deltakerlisteRepository.upsert(deltakerliste)) {
			is RepositoryResult.Created -> result.data
			is RepositoryResult.Modified -> result.data
			is RepositoryResult.NoChange -> deltakerliste
		}
}
