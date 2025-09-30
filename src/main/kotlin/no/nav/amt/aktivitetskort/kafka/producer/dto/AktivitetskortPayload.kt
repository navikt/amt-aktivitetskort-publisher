package no.nav.amt.aktivitetskort.kafka.producer.dto

import no.nav.amt.aktivitetskort.kafka.consumer.SOURCE
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakstype
import java.util.UUID

data class AktivitetskortPayload(
	val messageId: UUID,
	val source: String = SOURCE,
	val aktivitetskortType: Tiltakstype.ArenaKode,
	val actionType: String = "UPSERT_AKTIVITETSKORT_V1",
	val aktivitetskort: AktivitetskortDto,
)
