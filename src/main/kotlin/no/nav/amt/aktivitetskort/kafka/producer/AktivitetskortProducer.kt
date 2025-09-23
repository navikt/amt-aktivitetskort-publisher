package no.nav.amt.aktivitetskort.kafka.producer

import no.nav.amt.aktivitetskort.domain.Aktivitetskort
import no.nav.amt.aktivitetskort.kafka.consumer.AKTIVITETSKORT_TOPIC
import no.nav.amt.aktivitetskort.kafka.producer.dto.AktivitetskortPayload
import no.nav.amt.aktivitetskort.service.MetricsService
import no.nav.amt.aktivitetskort.utils.JsonUtils
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AktivitetskortProducer(
	private val template: KafkaTemplate<String, String>,
	private val metricsService: MetricsService,
) {
	private val log = LoggerFactory.getLogger(javaClass)

	fun send(aktivitetskort: Aktivitetskort) = send(listOf(aktivitetskort))

	fun send(aktivitetskort: List<Aktivitetskort>) {
		aktivitetskort.forEach {
			val messageId = UUID.randomUUID().toString()
			val payload = AktivitetskortPayload(
				messageId = UUID.randomUUID(),
				aktivitetskortType = it.tiltakstype.toArenaKode(),
				aktivitetskort = it.toAktivitetskortDto(),
			)
			template.send(AKTIVITETSKORT_TOPIC, it.id.toString(), JsonUtils.toJsonString(payload)).get()
			log.info("Sendte aktivtetskort til aktivitetsplanen: ${it.id}} messageId: $messageId")
			metricsService.incSendtAktivitetskort()
		}
	}
}
