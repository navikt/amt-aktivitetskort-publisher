package no.nav.amt.aktivitetskort.domain

import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.database.TestData
import org.junit.jupiter.api.Test

class AktivitetskortTest {
	@Test
	fun `lagTittel - deltakerliste og arrangor - lager riktig tittel basert på type tiltak`() {
		val arrangor = TestData.arrangor()
		val deltakerlister =
			Tiltak.Type.values().map {
				val tiltaksnavn = when (it) {
					Tiltak.Type.ARBFORB -> "Arbforb Tiltak"
					Tiltak.Type.ARBRRHDAG -> "ARR"
					Tiltak.Type.AVKLARAG -> "Avklaringstiltaket"
					Tiltak.Type.DIGIOPPARB -> "Digi. Oppfølging"
					Tiltak.Type.GRUPPEAMO -> "Grupper AMO"
					Tiltak.Type.JOBBK -> "Jobbklubben"
					Tiltak.Type.INDOPPFAG -> "Oppfølgingstiltak"
					Tiltak.Type.VASV -> "VTA"
					Tiltak.Type.GRUFAGYRKE -> "Gruppe yrkesfaglig utanning"
				}
				TestData.deltakerliste(tiltak = Tiltak(tiltaksnavn, it), arrangorId = arrangor.id)
			}

		deltakerlister.forEach {
			val aktivitetskortTittel = Aktivitetskort.lagTittel(it, arrangor)
			when (it.tiltak.type) {
				Tiltak.Type.DIGIOPPARB -> aktivitetskortTittel shouldBe "Digital oppfølging hos ${arrangor.navn}"
				Tiltak.Type.JOBBK -> aktivitetskortTittel shouldBe "Jobbsøkerkurs hos ${arrangor.navn}"
				Tiltak.Type.GRUPPEAMO -> aktivitetskortTittel shouldBe "Kurs: ${it.navn}"
				Tiltak.Type.GRUFAGYRKE -> aktivitetskortTittel shouldBe it.navn
				else -> aktivitetskortTittel shouldBe "${it.tiltak.navn} hos ${arrangor.navn}"
			}
		}
	}

	@Test
	fun `lagDetaljer - deltakerliste med deltakelsesmengde - lager detaljer i riktig rekkefølge`() {
		val deltakerliste = TestData.deltakerliste(
			tiltak = Tiltak("VTA 100%", Tiltak.Type.VASV),
		)
		val deltaker = TestData.deltaker(prosentStilling = 100.0, dagerPerUke = 2.5f, deltakerlisteId = deltakerliste.id)
		val arrangor = TestData.arrangor(id = deltakerliste.arrangorId)

		val detaljer = Aktivitetskort.lagDetaljer(deltaker, deltakerliste, arrangor)

		detaljer[0] shouldBe Detalj("Status for deltakelse", deltaker.status.display())
		detaljer[1] shouldBe Detalj("Deltakelsesmengde", "100%")
		detaljer[2] shouldBe Detalj("Arrangør", arrangor.navn)
	}

	@Test
	fun `lagDetaljer - deltaker ikke 100 prosent deltakelsesmengde - lager detalj med antall dager i uken`() {
		val deltakerliste = TestData.deltakerliste(
			tiltak = Tiltak("AFT 50% 2 dager i uken", Tiltak.Type.ARBFORB),
		)
		val deltaker = TestData.deltaker(prosentStilling = 50.0, dagerPerUke = 2.0f, deltakerlisteId = deltakerliste.id)
		val arrangor = TestData.arrangor(id = deltakerliste.arrangorId)

		val detaljer = Aktivitetskort.lagDetaljer(deltaker, deltakerliste, arrangor)

		detaljer.find { it.label == "Deltakelsesmengde" }!! shouldBe Detalj("Deltakelsesmengde", "50% 2 dager i uka")
	}

	@Test
	fun `lagDetaljer - deltaker ikke 100 prosent deltakelsesmengde og uten dager per uke - lager detalj med prosent`() {
		val deltakerliste = TestData.deltakerliste(
			tiltak = Tiltak("AFT 50%", Tiltak.Type.ARBFORB),
		)
		val arrangor = TestData.arrangor(id = deltakerliste.arrangorId)

		val deltaker1 = TestData.deltaker(prosentStilling = 50.0, dagerPerUke = 0.0f, deltakerlisteId = deltakerliste.id)
		val deltaker2 = TestData.deltaker(prosentStilling = 50.0, dagerPerUke = null, deltakerlisteId = deltakerliste.id)

		val detaljer1 = Aktivitetskort.lagDetaljer(deltaker1, deltakerliste, arrangor)
		val detaljer2 = Aktivitetskort.lagDetaljer(deltaker2, deltakerliste, arrangor)

		detaljer1.find { it.label == "Deltakelsesmengde" }!! shouldBe Detalj("Deltakelsesmengde", "50%")
		detaljer2.find { it.label == "Deltakelsesmengde" }!! shouldBe Detalj("Deltakelsesmengde", "50%")
	}

	@Test
	fun `lagDetaljer - deltaker uten prosentstilling - lager detalj med dager per uke`() {
		val deltakerliste = TestData.deltakerliste(
			tiltak = Tiltak("AFT noen dager", Tiltak.Type.ARBFORB),
		)
		val arrangor = TestData.arrangor(id = deltakerliste.arrangorId)

		val deltaker1 = TestData.deltaker(prosentStilling = 0.0, dagerPerUke = 5f, deltakerlisteId = deltakerliste.id)
		val deltaker2 = TestData.deltaker(prosentStilling = null, dagerPerUke = 1f, deltakerlisteId = deltakerliste.id)

		val detaljer1 = Aktivitetskort.lagDetaljer(deltaker1, deltakerliste, arrangor)
		val detaljer2 = Aktivitetskort.lagDetaljer(deltaker2, deltakerliste, arrangor)

		detaljer1.find { it.label == "Deltakelsesmengde" }!! shouldBe Detalj("Deltakelsesmengde", "5 dager i uka")
		detaljer2.find { it.label == "Deltakelsesmengde" }!! shouldBe Detalj("Deltakelsesmengde", "1 dag i uka")
	}

	@Test
	fun `lagDetaljer - deltaker med 0 prosent og 0 dager - lager ikke detalj med deltakelsesmengde`() {
		val deltakerliste = TestData.deltakerliste(
			tiltak = Tiltak("AFT", Tiltak.Type.ARBFORB),
		)
		val deltaker1 = TestData.deltaker(dagerPerUke = 0f, prosentStilling = 0.0)
		val deltaker2 = TestData.deltaker(dagerPerUke = null, prosentStilling = null)
		val arrangor = TestData.arrangor(id = deltakerliste.arrangorId)

		val detaljer1 = Aktivitetskort.lagDetaljer(deltaker1, deltakerliste, arrangor)
		val detaljer2 = Aktivitetskort.lagDetaljer(deltaker2, deltakerliste, arrangor)

		detaljer1.find { it.label == "Deltakelsesmengde" } shouldBe null
		detaljer2.find { it.label == "Deltakelsesmengde" } shouldBe null
	}

	@Test
	fun `lagDetaljer - tiltak uten deltakelsesmengde - lager ikke detalj med deltakelsesmengde`() {
		val deltakerliste = TestData.deltakerliste(
			tiltak = Tiltak("Oppfølgingstiltak", Tiltak.Type.INDOPPFAG),
		)
		val deltaker = TestData.deltaker()
		val arrangor = TestData.arrangor(id = deltakerliste.arrangorId)

		val detaljer = Aktivitetskort.lagDetaljer(deltaker, deltakerliste, arrangor)

		detaljer.find { it.label == "Deltakelsesmengde" } shouldBe null
	}
}
