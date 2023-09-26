package no.nav.amt.aktivitetskort.utils

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class StringUtilsKtTest {
	@Test
	fun `toTitleCase - et ord, bare store bokstaver - skal ha stor forbokstav og resten lower case`() {
		val storeBokstaver = "UPPERCASE"

		toTitleCase(storeBokstaver) shouldBe "Uppercase"
	}

	@Test
	fun `toTitleCase - et ord, bare sma bokstaver - skal ha stor forbokstav og resten lower case`() {
		val storeBokstaver = "lowercase"

		toTitleCase(storeBokstaver) shouldBe "Lowercase"
	}

	@Test
	fun `toTitleCase - to ord og AS, bare store bokstaver - skal ha stor forbokstaver og AS i store bokstaver`() {
		val storeBokstaver = "ARRANGØR AS"

		toTitleCase(storeBokstaver) shouldBe "Arrangør AS"
	}

	@Test
	fun `toTitleCase - flere ord med og, bare store bokstaver - skal formatteres riktig`() {
		val storeBokstaver = "ARRANGØR OG SØNN AS"

		toTitleCase(storeBokstaver) shouldBe "Arrangør og Sønn AS"
	}

	@Test
	fun `toTitleCase - flere ord med i, bare store bokstaver - skal formatteres riktig`() {
		val storeBokstaver = "ARRANGØR I BERGEN"

		toTitleCase(storeBokstaver) shouldBe "Arrangør i Bergen"
	}
}
