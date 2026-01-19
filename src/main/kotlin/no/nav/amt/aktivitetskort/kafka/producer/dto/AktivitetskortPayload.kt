package no.nav.amt.aktivitetskort.kafka.producer.dto

import no.nav.amt.aktivitetskort.domain.AktivitetskortTiltakstype
import no.nav.amt.aktivitetskort.kafka.consumer.SOURCE
import java.util.UUID

data class AktivitetskortPayload(
	val messageId: UUID,
	val source: String = SOURCE,
	val aktivitetskortType: AktivitetskortTiltakstype,
	val actionType: String = "UPSERT_AKTIVITETSKORT_V1",
	val aktivitetskort: AktivitetskortDto,
)
