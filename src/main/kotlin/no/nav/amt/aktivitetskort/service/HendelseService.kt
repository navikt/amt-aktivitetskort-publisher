package no.nav.amt.aktivitetskort.service

import no.nav.amt.aktivitetskort.domain.Arrangor
import no.nav.amt.aktivitetskort.domain.Deltaker
import no.nav.amt.aktivitetskort.domain.Deltakerliste
import no.nav.amt.aktivitetskort.kafka.consumer.dto.ArrangorDto
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerDto
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerlisteDto
import no.nav.amt.aktivitetskort.repositories.ArrangorRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerlisteRepository
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HendelseService(
	private val arrangorRepository: ArrangorRepository,
	private val deltakerlisteRepository: DeltakerlisteRepository,
	private val meldingService: MeldingService,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	fun deltakerHendelse(id: UUID, deltaker: DeltakerDto?) {
		if (deltaker == null) return

		val melding = meldingService.opprettMelding(deltaker.toModel())
		val result = meldingService.upsert(melding)

		when (result) {
			is RepositoryResult.Modified -> meldingService.publiserEndring(result.data)
			is RepositoryResult.Created -> meldingService.publiserEndring(result.data)
			is RepositoryResult.NoChange -> log.info("Ny hendelse for deltaker ${deltaker.id}: Ingen endring")
		}
		log.info("Konsumerte melding med deltaker $id")
	}

	fun deltakerlisteHendelse(id: UUID, deltakerliste: DeltakerlisteDto?) {
		if (deltakerliste == null) return

		val result = deltakerlisteRepository.upsert(deltakerliste.toModel())

		when (result) {
			is RepositoryResult.Modified -> meldingService.publiserEndring(result.data)
			is RepositoryResult.Created -> log.info("Ny hendelse deltakerliste ${deltakerliste.id}: Opprettet deltakerliste")
			is RepositoryResult.NoChange -> log.info("Ny hendelse for deltakerliste ${deltakerliste.id}: Ingen endring")
		}
		log.info("Konsumerte melding med deltakerliste $id")
	}

	fun arrangorHendelse(id: UUID, arrangor: ArrangorDto?) {
		if (arrangor == null) return

		val result = arrangorRepository.upsert(arrangor.toModel())

		when (result) {
			is RepositoryResult.Modified -> meldingService.publiserEndring(result.data)
			is RepositoryResult.Created -> log.info("Ny hendelse arrangør ${arrangor.id}: Opprettet arrangør")
			is RepositoryResult.NoChange -> log.info("Ny hendelse for arrangør ${arrangor.id}: Ingen endring")
		}
		log.info("Konsumerte melding med arrangør $id")
	}
}
