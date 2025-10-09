package no.nav.amt.aktivitetskort.kafka.consumer

import no.nav.amt.aktivitetskort.kafka.TiltakstypeUtils.tiltakskodeErStottet
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerlisteDto
import no.nav.amt.aktivitetskort.service.FeilmeldingService
import no.nav.amt.aktivitetskort.service.KafkaConsumerService
import no.nav.amt.aktivitetskort.utils.JsonUtils.fromJson
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
		topics = [DELTAKER_TOPIC, ARRANGOR_TOPIC, DELTAKERLISTE_TOPIC_V1, FEIL_TOPIC],
		containerFactory = "kafkaListenerContainerFactory",
	)
	fun listen(record: ConsumerRecord<String, String>, ack: Acknowledgment) {
		when (record.topic()) {
			ARRANGOR_TOPIC -> kafkaConsumerService.arrangorHendelse(
				UUID.fromString(record.key()),
				record.value()?.let { fromJson(it) },
			)

			DELTAKERLISTE_TOPIC_V1 -> {
				record
					.value()
					?.let { fromJson<DeltakerlisteDto>(it) }
					?.takeIf { deltakerlisteDto -> tiltakskodeErStottet(deltakerlisteDto.tiltakstype.tiltakskode) }
					?.let { deltakerliste ->
						kafkaConsumerService.deltakerlisteHendelse(
							id = UUID.fromString(record.key()),
							deltakerlisteDto = deltakerliste,
						)
					}
			}

/*
			DELTAKERLISTE_TOPIC_V2 -> kafkaConsumerService.deltakerlisteHendelse(
				UUID.fromString(record.key()),
				record.value()?.let { fromJson(it) },
			)
*/

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
