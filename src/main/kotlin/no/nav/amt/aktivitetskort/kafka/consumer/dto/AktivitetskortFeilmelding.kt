package no.nav.amt.aktivitetskort.kafka.consumer.dto

import java.time.LocalDateTime
import java.util.UUID

data class AktivitetskortFeilmelding(
	val key: UUID,
	val source: String,
	val timestamp: LocalDateTime,
	val failingMessage: String,
	val errorMessage: String,
	val errorType: String,
)
