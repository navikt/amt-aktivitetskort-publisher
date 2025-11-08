package no.nav.amt.aktivitetskort.utils

import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.shouldBe
import no.nav.amt.lib.utils.objectMapper
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class JsonUtilsTest {
	@Test
	fun `ZonedDateTime til LocalDateTime-tester`() {
		val localDateTimeInTest = LocalDateTime.of(2025, 7, 26, 0, 0, 0)

		// Oppretter en ZonedDateTime med Oslo-tidssone
		val zonedDateTimeInTest = ZonedDateTime.of(localDateTimeInTest, ZoneId.of("Europe/Oslo"))
		println(zonedDateTimeInTest) // 2025-07-26T00:00+02:00[Europe/Oslo]

		// Serialiserer ZonedDateTime til JSON
		// NB: Tidssonenavn ([Europe/Oslo]) går tapt, kun offset (+02:00) beholdes
		val zdtJson = objectMapper.writeValueAsString(zonedDateTimeInTest)
		zdtJson shouldBe "\"2025-07-26T00:00:00+02:00\""

		// Deserialiserer JSON tilbake til ZonedDateTime
		// NB: JSON-tidspunktet tolkes som +02:00, men Instant beholdes korrekt
		val zdtFromJson = objectMapper.readValue<ZonedDateTime>(zdtJson)
		println(zdtFromJson) // 2025-07-25T22:00Z (samme tidspunkt som originalt, men nå i UTC)
		zdtFromJson.toInstant() shouldBe zonedDateTimeInTest.toInstant()

		// Feil: Konverterer direkte til LocalDateTime og mister offset
		val wrongLocalDateTimeFromZdt = zdtFromJson.toLocalDateTime()
		println(wrongLocalDateTimeFromZdt) // 2025-07-25T22:00 → FEIL: 2 timer for tidlig
		wrongLocalDateTimeFromZdt shouldBe localDateTimeInTest.minusHours(2)

		// Korrekt fremgangsmåte: Justerer til systemets tidssone før konvertering
		val correctLocalDateTimeFromZdt = zdtFromJson
			.withZoneSameInstant(ZoneId.of("Europe/Oslo"))
			.toLocalDateTime()
		println(correctLocalDateTimeFromZdt) // 2025-07-26T00:00 RIKTIG
		correctLocalDateTimeFromZdt shouldBe localDateTimeInTest
	}
}
