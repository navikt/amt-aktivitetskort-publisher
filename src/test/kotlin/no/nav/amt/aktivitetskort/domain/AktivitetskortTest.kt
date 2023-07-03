package no.nav.amt.aktivitetskort.domain

import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.database.TestData
import org.junit.jupiter.api.Test

class AktivitetskortTest {
	@Test
	fun `lagTittel - deltakerliste og arrangor - lager riktig tittel basert på type tiltak`() {
		val arrangor = TestData.arrangor()
		val deltakerlister =
			Tiltak.Type.values().map { TestData.deltakerliste(tiltak = Tiltak(it.name, it), arrangorId = arrangor.id) }

		deltakerlister.forEach {
			val aktivitetskortTittel = Aktivitetskort.lagTittel(it, arrangor)
			when (it.tiltak.type) {
				Tiltak.Type.DIGITALT_OPPFOELGINGSTILTAK -> aktivitetskortTittel shouldBe "Digital oppfølging hos ${arrangor.navn}"
				Tiltak.Type.JOBBKLUBB -> aktivitetskortTittel shouldBe "Jobbsøkerkurs hos ${arrangor.navn}"
				Tiltak.Type.ARBEIDSMARKEDSOPPLAERING -> aktivitetskortTittel shouldBe "Kurs: ${it.navn}"
				else -> aktivitetskortTittel shouldBe "${it.tiltak.type} hos ${arrangor.navn}"
			}
		}
	}
}
