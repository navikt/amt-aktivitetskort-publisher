package no.nav.amt.aktivitetskort.kafka.consumer.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.json.schema.shouldMatchSchema
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.database.TestData.arrangor
import no.nav.amt.aktivitetskort.kafka.consumer.dto.DeltakerlistePayloadJsonSchemas.deltakerlistePayloadV2Schema
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode
import no.nav.amt.lib.utils.objectMapper
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class DeltakerlistePayloadTest {
	@Nested
	inner class Tiltakskodenavn {
		@Test
		fun `organisasjonsnummer - kaster feil hvis tiltakskode mangler`() {
			val payload = DeltakerlistePayload(
				id = deltakerlisteIdInTest,
				arrangor = DeltakerlistePayload.Arrangor("123456789"),
			)

			shouldThrow<IllegalStateException> {
				payload.effectiveTiltakskode
			}
		}

		@Test
		fun `returnerer tiltakskode fra Tiltakstype`() {
			val expectedTiltakskode = Tiltakskode.ENKELTPLASS_FAG_OG_YRKESOPPLAERING.name

			val payload = DeltakerlistePayload(
				id = deltakerlisteIdInTest,
				tiltakstype = DeltakerlistePayload.Tiltakstype(expectedTiltakskode),
				arrangor = DeltakerlistePayload.Arrangor("987654321"),
			)

			payload.effectiveTiltakskode shouldBe expectedTiltakskode
		}

		@Test
		fun `returnerer tiltakskode fra tiltakskode`() {
			val expectedTiltakskode = Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING.name

			val payload = DeltakerlistePayload(
				id = deltakerlisteIdInTest,
				tiltakskode = expectedTiltakskode,
				arrangor = DeltakerlistePayload.Arrangor("987654321"),
			)

			payload.effectiveTiltakskode shouldBe expectedTiltakskode
		}
	}

	@Nested
	inner class ToModel {
		@Test
		fun `toModel - mapper felter korrekt`() {
			val payload = fullyPopulatedV2PayloadInTest.copy()

			val expectedArrangorId = UUID.randomUUID()

			val model = payload.toModel(arrangorId = expectedArrangorId, navnTiltakstype = EXPECTED_NAVN_TILTAKSTYPE)

			assertSoftly(model) {
				tiltak.tiltakskode shouldBe expectedTiltakskode
				arrangorId shouldBe expectedArrangorId

				id shouldBe id
				navn shouldBe "Testliste"
			}
		}

		@Test
		fun `toModel - bruker tiltakstype-navn hvis navn er null`() {
			val payload = DeltakerlistePayload(
				id = deltakerlisteIdInTest,
				tiltakstype = DeltakerlistePayload.Tiltakstype(Tiltakskode.ENKELTPLASS_FAG_OG_YRKESOPPLAERING.name),
				arrangor = DeltakerlistePayload.Arrangor("987654321"),
			)

			val arrangor = arrangor()

			val model = payload.toModel(arrangorId = arrangor.id, navnTiltakstype = "Test tiltak ENKFAGYRKE")

			model.navn shouldBe "Test tiltak ENKFAGYRKE"
		}
	}

	@Nested
	inner class Validate {
		@Test
		fun `fullt populert V2 skal matche skjema`() {
			val json = objectMapper
				.copy()
				.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
				.writeValueAsString(fullyPopulatedV2PayloadInTest.copy())

			json.shouldMatchSchema(deltakerlistePayloadV2Schema)
		}
	}

	companion object {
		private val deltakerlisteIdInTest = UUID.randomUUID()
		private val expectedTiltakskode = Tiltakskode.ENKELTPLASS_FAG_OG_YRKESOPPLAERING
		private const val EXPECTED_NAVN_TILTAKSTYPE = "Test tiltak ENKFAGYRKE"

		private val fullyPopulatedV2PayloadInTest = DeltakerlistePayload(
			id = deltakerlisteIdInTest,
			tiltakskode = expectedTiltakskode.name,
			tiltakstype = DeltakerlistePayload.Tiltakstype(expectedTiltakskode.name),
			navn = "Testliste",
			arrangor = DeltakerlistePayload.Arrangor("123456789"),
		)
	}
}
