package no.nav.amt.aktivitetskort.kafka.consumer

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerlistePayload
import no.nav.amt.aktivitetskort.service.FeilmeldingService
import no.nav.amt.aktivitetskort.service.KafkaConsumerService
import no.nav.amt.aktivitetskort.utils.JsonUtils.fromJson
import no.nav.amt.lib.utils.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class KafkaConsumer(
	val kafkaConsumerService: KafkaConsumerService,
	val feilmeldingService: FeilmeldingService,
) {
	@KafkaListener(
		topics = [DELTAKER_TOPIC, ARRANGOR_TOPIC, DELTAKERLISTE_TOPIC_V1, DELTAKERLISTE_TOPIC_V2, FEIL_TOPIC],
		containerFactory = "kafkaListenerContainerFactory",
	)
	fun listen(record: ConsumerRecord<String, String>, ack: Acknowledgment) {
		when (record.topic()) {
			ARRANGOR_TOPIC -> kafkaConsumerService.arrangorHendelse(
				UUID.fromString(record.key()),
				record.value()?.let { fromJson(it) },
			)

			// fjernes ved overgang til v2
			DELTAKERLISTE_TOPIC_V1 -> {
				objectMapper
					.readValue<DeltakerlistePayload>(record.value())
					.takeIf { it.tiltakstype.erStottet() }
					?.let { kafkaConsumerService.deltakerlisteHendelse(deltakerlistePayload = it) }
			}

			DELTAKERLISTE_TOPIC_V2 -> {
				objectMapper
					.readValue<DeltakerlistePayload>(record.value())
					.takeIf { it.tiltakstype.erStottet() }
					?.let { kafkaConsumerService.deltakerlisteHendelse(deltakerlistePayload = it) }
			}

			DELTAKER_TOPIC -> kafkaConsumerService.deltakerHendelse(
				id = UUID.fromString(record.key()),
				deltaker = record.value()?.let { fromJson(it) },
				offset = record.offset(),
			)

			FEIL_TOPIC -> feilmeldingService.handleFeilmelding(
				UUID.fromString(record.key()),
				record.value()?.let { fromJson(it) },
			)
		}

		ack.acknowledge()
	}
}
