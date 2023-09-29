package no.nav.amt.aktivitetskort.service

import no.nav.amt.aktivitetskort.client.AmtArrangorClient
import no.nav.amt.aktivitetskort.domain.Aktivitetskort
import no.nav.amt.aktivitetskort.kafka.consumer.AKTIVITETSKORT_TOPIC
import no.nav.amt.aktivitetskort.kafka.consumer.dto.ArrangorDto
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerDto
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerlisteDto
import no.nav.amt.aktivitetskort.kafka.producer.dto.AktivitetskortPayload
import no.nav.amt.aktivitetskort.repositories.ArrangorRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerlisteRepository
import no.nav.amt.aktivitetskort.service.StatusMapping.deltakerStatusTilAktivetStatus
import no.nav.amt.aktivitetskort.utils.JsonUtils
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.UUID

@Service
class HendelseService(
	private val arrangorRepository: ArrangorRepository,
	private val deltakerlisteRepository: DeltakerlisteRepository,
	private val deltakerRepository: DeltakerRepository,
	private val aktivitetskortService: AktivitetskortService,
	private val amtArrangorClient: AmtArrangorClient,
	private val template: KafkaTemplate<String, String>,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	fun deltakerHendelse(id: UUID, deltaker: DeltakerDto?) {
		if (deltaker == null) return

		if (deltakerStatusTilAktivetStatus(deltaker.status.type).isFailure) {
			log.info("Kan ikke lage aktivitetskort for deltaker ${deltaker.id} med status ${deltaker.status.type}")
			return
		}

		when (val result = deltakerRepository.upsert(deltaker.toModel())) {
			is RepositoryResult.Modified -> send(aktivitetskortService.lagAktivitetskort(result.data))
			is RepositoryResult.Created -> send(aktivitetskortService.lagAktivitetskort(result.data))
			is RepositoryResult.NoChange -> log.info("Ny hendelse for deltaker ${deltaker.id}: Ingen endring")
		}
		log.info("Konsumerte melding med deltaker $id")
	}

	fun deltakerlisteHendelse(id: UUID, deltakerliste: DeltakerlisteDto?) {
		if (deltakerliste == null) return

		if (!deltakerliste.tiltakstype.erStottet()) return

		val arrangor = arrangorRepository.get(deltakerliste.virksomhetsnummer)
			?: amtArrangorClient.hentArrangor(deltakerliste.virksomhetsnummer)

		when (val result = deltakerlisteRepository.upsert(deltakerliste.toModel(arrangor.id))) {
			is RepositoryResult.Modified -> send(aktivitetskortService.lagAktivitetskort(result.data))
			is RepositoryResult.Created -> log.info("Ny hendelse deltakerliste ${deltakerliste.id}: Opprettet deltakerliste")
			is RepositoryResult.NoChange -> log.info("Ny hendelse for deltakerliste ${deltakerliste.id}: Ingen endring")
		}
		log.info("Konsumerte melding med deltakerliste $id")
	}

	fun arrangorHendelse(id: UUID, arrangor: ArrangorDto?) {
		if (arrangor == null) return

		when (val result = arrangorRepository.upsert(arrangor.toModel())) {
			is RepositoryResult.Modified -> send(aktivitetskortService.lagAktivitetskort(result.data))
			is RepositoryResult.Created -> log.info("Ny hendelse arrangør ${arrangor.id}: Opprettet arrangør")
			is RepositoryResult.NoChange -> log.info("Ny hendelse for arrangør ${arrangor.id}: Ingen endring")
		}
		log.info("Konsumerte melding med arrangør $id")
	}

	private fun send(aktivitetskort: Aktivitetskort) = send(listOf(aktivitetskort))

	private fun send(aktivitetskort: List<Aktivitetskort>) {
		aktivitetskort.forEach {
			val payload = AktivitetskortPayload(
				messageId = UUID.randomUUID(),
				sendt = ZonedDateTime.now(),
				aktivitetskortType = it.tiltakstype,
				aktivitetskort = it.toAktivitetskortDto(),
			)
			template.send(AKTIVITETSKORT_TOPIC, it.id.toString(), JsonUtils.toJsonString(payload)).get()
		}
		log.info("Sendte aktivtetskort til aktivitetsplanen: ${aktivitetskort.joinToString { it.id.toString() }}")
	}
}
