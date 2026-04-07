package no.nav.amt.aktivitetskort.kafka.producer.dto

import no.nav.amt.aktivitetskort.domain.AktivitetskortTiltakstype
import no.nav.amt.aktivitetskort.kafka.consumer.SOURCE
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID

data class AktivitetskortPayload(
	val messageId: UUID,
	val source: String = SOURCE,
	val aktivitetskortType: AktivitetskortTiltakstype,
	val actionType: String = "UPSERT_AKTIVITETSKORT_V1",
	val aktivitetskort: AktivitetskortDto,
)

data class AktivitetskortKasseringPayload(
	val messageId: UUID,
	val aktivitetsId: UUID,
	val actionType: String = "KASSER_AKTIVITET",
	val personIdent: String,
	val navIdent: String,
	val begrunnelse: String,
	val sendt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
	val source: String = SOURCE,
)
