package no.nav.amt.aktivitetskort.service

import io.getunleash.DefaultUnleash
import no.nav.amt.aktivitetskort.client.AmtArrangorClient
import no.nav.amt.aktivitetskort.domain.AktivitetStatus
import no.nav.amt.aktivitetskort.domain.Aktivitetskort
import no.nav.amt.aktivitetskort.domain.Arrangor
import no.nav.amt.aktivitetskort.domain.DeltakerStatus
import no.nav.amt.aktivitetskort.kafka.consumer.AKTIVITETSKORT_TOPIC
import no.nav.amt.aktivitetskort.kafka.consumer.dto.ArrangorDto
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerDto
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerlisteDto
import no.nav.amt.aktivitetskort.kafka.producer.dto.AktivitetskortPayload
import no.nav.amt.aktivitetskort.repositories.ArrangorRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerRepository
import no.nav.amt.aktivitetskort.repositories.DeltakerlisteRepository
import no.nav.amt.aktivitetskort.service.StatusMapping.deltakerStatusTilAktivitetStatus
import no.nav.amt.aktivitetskort.utils.JsonUtils
import no.nav.amt.aktivitetskort.utils.RepositoryResult
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HendelseService(
	private val arrangorRepository: ArrangorRepository,
	private val deltakerlisteRepository: DeltakerlisteRepository,
	private val deltakerRepository: DeltakerRepository,
	private val aktivitetskortService: AktivitetskortService,
	private val amtArrangorClient: AmtArrangorClient,
	private val template: KafkaTemplate<String, String>,
	private val unleash: DefaultUnleash,
	private val metricsService: MetricsService,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	fun deltakerHendelse(id: UUID, deltaker: DeltakerDto?, offset: Long) {
		if (deltaker == null) return handterSlettetDeltaker(id)

		if (deltakerStatusTilAktivitetStatus(deltaker.status.type).isFailure) {
			log.info("Kan ikke lage aktivitetskort for deltaker ${deltaker.id} med status ${deltaker.status.type}")
			return
		}

		when (val result = deltakerRepository.upsert(deltaker.toModel(), offset, skalRelasteDeltakere())) {
			is RepositoryResult.Modified -> send(aktivitetskortService.lagAktivitetskort(result.data))
				.also { log.info("Oppdatert deltaker: $id") }
			is RepositoryResult.Created -> send(aktivitetskortService.lagAktivitetskort(result.data)).also {
				log.info("Opprettet deltaker: $id")
			}
			is RepositoryResult.NoChange -> log.info("Ny hendelse for deltaker ${deltaker.id}: Ingen endring")
		}
		log.info("Konsumerte melding med deltaker $id, offset $offset")
	}

	fun deltakerlisteHendelse(id: UUID, deltakerliste: DeltakerlisteDto?) {
		if (deltakerliste == null) return

		if (!deltakerliste.tiltakstype.erStottet()) return

		val arrangor = arrangorRepository.get(deltakerliste.virksomhetsnummer)
			?: hentOgLagreArrangorFraAmtArrangor(deltakerliste.virksomhetsnummer)

		when (val result = deltakerlisteRepository.upsert(deltakerliste.toModel(arrangor.id))) {
			is RepositoryResult.Modified -> send(aktivitetskortService.lagAktivitetskort(result.data))
			is RepositoryResult.Created -> log.info("Ny hendelse deltakerliste ${deltakerliste.id}: Opprettet deltakerliste")
			is RepositoryResult.NoChange -> log.info("Ny hendelse for deltakerliste ${deltakerliste.id}: Ingen endring")
		}
		log.info("Konsumerte melding med deltakerliste $id")
	}

	fun arrangorHendelse(id: UUID, arrangor: ArrangorDto?) {
		if (arrangor == null) return
		if (arrangor.overordnetArrangorId != null && arrangorRepository.get(arrangor.overordnetArrangorId) == null) {
			hentOgLagreArrangorFraAmtArrangor(arrangor.overordnetArrangorId)
		}

		when (val result = arrangorRepository.upsert(arrangor.toModel())) {
			is RepositoryResult.Modified -> send(aktivitetskortService.lagAktivitetskort(result.data))
			is RepositoryResult.Created -> log.info("Ny hendelse arrangør ${arrangor.id}: Opprettet arrangør")
			is RepositoryResult.NoChange -> log.info("Ny hendelse for arrangør ${arrangor.id}: Ingen endring")
		}
		log.info("Konsumerte melding med arrangør $id")
	}

	private fun send(aktivitetskort: Aktivitetskort) = send(listOf(aktivitetskort))

	private fun send(aktivitetskort: List<Aktivitetskort>) {
		if (unleash.isEnabled("amt.send-aktivitetskort")) {
			aktivitetskort.forEach {
				val payload = AktivitetskortPayload(
					messageId = UUID.randomUUID(),
					aktivitetskortType = it.tiltakstype,
					aktivitetskort = it.toAktivitetskortDto(),
				)
				template.send(AKTIVITETSKORT_TOPIC, it.id.toString(), JsonUtils.toJsonString(payload)).get()
				metricsService.incSendtAktivitetskort()
			}
			log.info("Sendte aktivtetskort til aktivitetsplanen: ${aktivitetskort.joinToString { it.id.toString() }}")
		} else {
			log.info("Sender ikke aktivitetskort fordi funksjonaliteten er togglet av")
		}
	}

	private fun hentOgLagreArrangorFraAmtArrangor(virksomhetsnummer: String): Arrangor {
		val arrangorMedOverordnetArrangor = amtArrangorClient.hentArrangor(virksomhetsnummer)
		lagreArrangorMedOverordnetArrangor(arrangorMedOverordnetArrangor)
		return arrangorRepository.get(virksomhetsnummer)
			?: throw RuntimeException("Fant ikke arrangør med id ${arrangorMedOverordnetArrangor.id} som vi nettopp lagret")
	}

	private fun hentOgLagreArrangorFraAmtArrangor(arrangorId: UUID) {
		val arrangorMedOverordnetArrangor = amtArrangorClient.hentArrangor(arrangorId)
		lagreArrangorMedOverordnetArrangor(arrangorMedOverordnetArrangor)
		log.info("Hentet og lagret overordnet arrangør med id $arrangorId som manglet i databasen")
	}

	private fun lagreArrangorMedOverordnetArrangor(arrangorMedOverordnetArrangor: AmtArrangorClient.ArrangorMedOverordnetArrangorDto) {
		arrangorMedOverordnetArrangor.overordnetArrangor?.let {
			arrangorRepository.upsert(
				Arrangor(
					id = it.id,
					organisasjonsnummer = it.organisasjonsnummer,
					navn = it.navn,
					overordnetArrangorId = it.overordnetArrangorId,
				),
			)
		}
		arrangorRepository.upsert(
			Arrangor(
				id = arrangorMedOverordnetArrangor.id,
				organisasjonsnummer = arrangorMedOverordnetArrangor.organisasjonsnummer,
				navn = arrangorMedOverordnetArrangor.navn,
				overordnetArrangorId = arrangorMedOverordnetArrangor.overordnetArrangor?.id,
			),
		)
	}

	private fun handterSlettetDeltaker(deltakerId: UUID) {
		val deltaker = deltakerRepository.get(deltakerId) ?: return
		val aktivitetStatus = aktivitetskortService.getMelding(deltaker.id)?.aktivitetskort?.aktivitetStatus

		if (skalAvbryteAktivtetskort(aktivitetStatus)) {
			val avbruttDeltaker = deltaker.copy(status = DeltakerStatus(DeltakerStatus.Type.AVBRUTT, null))

			send(aktivitetskortService.lagAktivitetskort(avbruttDeltaker))
			log.info(
				"Mottok tombstone for deltaker: $deltakerId som hadde status: ${deltaker.status.type}. " +
					"Avbrøt deltakelse og aktivitetskort.",
			)
		}

		log.info("Mottok tombstone for deltaker: $deltakerId og slettet deltaker")
		deltakerRepository.delete(deltakerId)
	}

	private fun skalAvbryteAktivtetskort(status: AktivitetStatus?): Boolean {
		return when (status) {
			AktivitetStatus.FORSLAG,
			AktivitetStatus.PLANLAGT,
			AktivitetStatus.GJENNOMFORES,
			-> true

			else -> false
		}
	}

	private fun skalRelasteDeltakere(): Boolean {
		return unleash.isEnabled("amt.relast-aktivitetskort-deltaker")
	}
}
