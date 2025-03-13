package no.nav.amt.aktivitetskort.kafka.consumer

import no.nav.amt.aktivitetskort.service.FeilmeldingService
import no.nav.amt.aktivitetskort.service.KafkaConsumerService
import no.nav.amt.aktivitetskort.utils.JsonUtils.fromJson
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.UUID

const val DELTAKER_TOPIC = "amt.deltaker-v2"
const val ARRANGOR_TOPIC = "amt.arrangor-v1"
const val DELTAKERLISTE_TOPIC = "team-mulighetsrommet.siste-tiltaksgjennomforinger-v1"
const val FEILTOPIC = "dab.aktivitetskort-feil-v1"
const val AKTIVITETSKORT_TOPIC = "dab.aktivitetskort-v1.1"

const val SOURCE = "TEAM_KOMET"

@Component
class KafkaConsumer(
	val kafkaConsumerService: KafkaConsumerService,
	val feilmeldingService: FeilmeldingService,
) {
	@KafkaListener(
		topics = [DELTAKER_TOPIC, ARRANGOR_TOPIC, DELTAKERLISTE_TOPIC, FEILTOPIC],
		containerFactory = "kafkaListenerContainerFactory",
	)
	fun listen(record: ConsumerRecord<String, String>, ack: Acknowledgment) {
		when (record.topic()) {
			ARRANGOR_TOPIC -> kafkaConsumerService.arrangorHendelse(
				UUID.fromString(record.key()),
				record.value()?.let { fromJson(it) },
			)

			DELTAKERLISTE_TOPIC -> kafkaConsumerService.deltakerlisteHendelse(
				UUID.fromString(record.key()),
				record.value()?.let { fromJson(it) },
			)

			DELTAKER_TOPIC -> kafkaConsumerService.deltakerHendelse(
				id = UUID.fromString(record.key()),
				deltaker = record.value()?.let { fromJson(it) },
				offset = record.offset(),
			)

			FEILTOPIC -> feilmeldingService.handleFeilmelding(
				UUID.fromString(record.key()),
				record.value()?.let { fromJson(it) },
			)
		}

		ack.acknowledge()
	}
}
