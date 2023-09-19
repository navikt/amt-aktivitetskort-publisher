package no.nav.amt.aktivitetskort.service

import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.IntegrationTest
import no.nav.amt.aktivitetskort.database.TestDatabaseService
import no.nav.amt.aktivitetskort.kafka.consumer.SOURCE
import no.nav.amt.aktivitetskort.kafka.consumer.dto.AktivitetskortFeilmelding
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime
import java.util.UUID

class FeilmeldingServiceTest : IntegrationTest() {
	@Autowired
	lateinit var feilmeldingService: FeilmeldingService

	@Autowired
	lateinit var db: TestDatabaseService

	@Test
	fun `handleFeilmelding - feilmelding med TEAM_KOMET som source - feilmelding lagres`() {
		val key = UUID.randomUUID()
		val feilmelding = AktivitetskortFeilmelding(
			key = key,
			source = SOURCE,
			timestamp = ZonedDateTime.now(),
			failingMessage = "melding som feiler",
			errorMessage = "DeserialiseringsFeil Meldingspayload er ikke gyldig json",
			errorType = "DESERIALISERINGSFEIL",
		)

		feilmeldingService.handleFeilmelding(key, feilmelding)

		db.feilmeldingErLagret(key) shouldBe true
	}

	@Test
	fun `handleFeilmelding - feilmelding med TEAM_TILTAK som source - feilmelding lagres ikke`() {
		val key = UUID.randomUUID()
		val feilmelding = AktivitetskortFeilmelding(
			key = key,
			source = "TEAM_TILTAK",
			timestamp = ZonedDateTime.now(),
			failingMessage = "melding som feiler",
			errorMessage = "DeserialiseringsFeil Meldingspayload er ikke gyldig json",
			errorType = "DESERIALISERINGSFEIL",
		)

		feilmeldingService.handleFeilmelding(key, feilmelding)

		db.feilmeldingErLagret(key) shouldBe false
	}
}
