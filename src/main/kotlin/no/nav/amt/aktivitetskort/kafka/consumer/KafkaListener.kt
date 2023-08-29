package no.nav.amt.aktivitetskort.kafka.consumer

import no.nav.amt.aktivitetskort.service.HendelseService
import no.nav.amt.aktivitetskort.utils.JsonUtils.fromJson
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.UUID

const val DELTAKER_TOPIC = "amt.deltaker-v2"
const val ARRANGOR_TOPIC = "amt.arrangor-v1"
const val DELTAKERLISTE_TOPIC = "amt.deltakerliste-v1"

@Component
class KafkaListener(
	val hendelseService: HendelseService,
) {

	@KafkaListener(
		topics = [DELTAKER_TOPIC, ARRANGOR_TOPIC, DELTAKERLISTE_TOPIC],
		containerFactory = "kafkaListenerContainerFactory",
	)
	fun listen(record: ConsumerRecord<String, String>, ack: Acknowledgment) {
		when (record.topic()) {
			ARRANGOR_TOPIC -> hendelseService.arrangorHendelse(
				UUID.fromString(record.key()),
				record.value()?.let { fromJson(it) },
			)

			DELTAKERLISTE_TOPIC -> hendelseService.deltakerlisteHendelse(
				UUID.fromString(record.key()),
				record.value()?.let { fromJson(it) },
			)

			DELTAKER_TOPIC -> hendelseService.deltakerHendelse(
				UUID.fromString(record.key()),
				record.value()?.let { fromJson(it) },
			)
		}

		ack.acknowledge()
	}
}
