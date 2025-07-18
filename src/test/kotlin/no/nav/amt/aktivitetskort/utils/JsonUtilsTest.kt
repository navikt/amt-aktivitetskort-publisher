package no.nav.amt.aktivitetskort.utils

import com.fasterxml.jackson.databind.RuntimeJsonMappingException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.client.AmtArrangorClient.ArrangorMedOverordnetArrangorDto
import no.nav.amt.aktivitetskort.utils.JsonUtils.fromJson
import org.junit.jupiter.api.Test
import java.util.UUID

class JsonUtilsTest {
	@Test
	fun `skal returnere UUID for gyldig UUID-streng i JSON`() {
		val expectedUUID = UUID.randomUUID()
		val actualUUID = fromJson<UUID?>("\"$expectedUUID\"")
		actualUUID shouldBe expectedUUID
	}

	@Test
	fun `skal kaste feil hvis JSON ikke kan parses til spesifisert type`() {
		val thrown = shouldThrow<RuntimeJsonMappingException> {
			fromJson<ArrangorMedOverordnetArrangorDto>("null")
		}

		thrown.message shouldBe
			"Deserialized value did not match the specified type; specified no.nav.amt.aktivitetskort.client.AmtArrangorClient.ArrangorMedOverordnetArrangorDto(non-null) but was null"
	}

	@Test
	fun `skal ikke kaste feil for nullable type med null-JSON`() {
		val result = fromJson<ArrangorMedOverordnetArrangorDto?>("null")
		result.shouldBeNull()
	}
}
