package no.nav.amt.aktivitetskort.domain

import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode
import org.junit.jupiter.api.Test

class AktivitetskortTest {
	@Test
	fun `lagTittel - deltakerliste og arrangor - lager riktig tittel basert på type tiltak`() {
		val arrangor = TestData.arrangor()
		val deltakerlister =
			Tiltakskode.entries.map {
				val tiltaksnavn = when (it) {
					Tiltakskode.ARBEIDSFORBEREDENDE_TRENING -> "Arbforb Tiltak"
					Tiltakskode.ARBEIDSRETTET_REHABILITERING -> "ARR"
					Tiltakskode.AVKLARING -> "Avklaringstiltaket"
					Tiltakskode.DIGITALT_OPPFOLGINGSTILTAK -> "Digitalt jobbsøkerkurs"
					Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING -> "Grupper AMO"
					Tiltakskode.JOBBKLUBB -> "Jobbklubben"
					Tiltakskode.OPPFOLGING -> "Oppfølgingstiltak"
					Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET -> "VTA"
					Tiltakskode.GRUPPE_FAG_OG_YRKESOPPLAERING -> "Gruppe yrkesfaglig utanning"
					Tiltakskode.AMO -> "Arbeidsmarkedsopplæring"
					Tiltakskode.FAG_OG_YRKESOPPLAERING -> "Fag og yrkesopplæring"
					Tiltakskode.HOYERE_UTDANNING -> "Høyere utdanning"
				}
				TestData.deltakerliste(tiltak = Tiltak(tiltaksnavn, it), arrangorId = arrangor.id)
			}

		deltakerlister.forEach {
			val aktivitetskortTittel = Aktivitetskort.lagTittel(it, arrangor, true)
			when (it.tiltak.tiltakskode) {
				Tiltakskode.DIGITALT_OPPFOLGINGSTILTAK -> aktivitetskortTittel shouldBe "Digitalt jobbsøkerkurs hos ${arrangor.navn}"
				Tiltakskode.JOBBKLUBB -> aktivitetskortTittel shouldBe "Jobbsøkerkurs hos ${arrangor.navn}"
				Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET -> aktivitetskortTittel shouldBe "Tilrettelagt arbeid hos ${arrangor.navn}"
				Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING -> aktivitetskortTittel shouldBe "Kurs: ${it.navn}"
				Tiltakskode.GRUPPE_FAG_OG_YRKESOPPLAERING -> aktivitetskortTittel shouldBe it.navn
				Tiltakskode.AMO -> aktivitetskortTittel shouldBe "Arbeidsmarkedsopplæring"
				Tiltakskode.FAG_OG_YRKESOPPLAERING -> aktivitetskortTittel shouldBe "Fag og yrkesopplæring"
				Tiltakskode.HOYERE_UTDANNING -> aktivitetskortTittel shouldBe "Høyere utdanning"
				else -> aktivitetskortTittel shouldBe "${it.tiltak.navn} hos ${arrangor.navn}"
			}
		}
		deltakerlister.find { it.tiltak.tiltakskode == Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING }?.let {
			val aktivitetskortTittel = Aktivitetskort.lagTittel(it, arrangor, false)
			aktivitetskortTittel shouldBe it.navn
		}
	}

	@Test
	fun `lagDetaljer - deltakerliste med deltakelsesmengde - lager detaljer i riktig rekkefølge`() {
		val deltakerliste = TestData.deltakerliste(
			tiltak = Tiltak("VTA 100%", Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET),
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
			tiltak = Tiltak("AFT 50% 2 dager i uken", Tiltakskode.ARBEIDSFORBEREDENDE_TRENING),
		)
		val deltaker = TestData.deltaker(prosentStilling = 50.0, dagerPerUke = 2.0f, deltakerlisteId = deltakerliste.id)
		val arrangor = TestData.arrangor(id = deltakerliste.arrangorId)

		val detaljer = Aktivitetskort.lagDetaljer(deltaker, deltakerliste, arrangor)

		detaljer.first { it.label == "Deltakelsesmengde" } shouldBe Detalj("Deltakelsesmengde", "50% fordelt på 2 dager i uka")
	}

	@Test
	fun `lagDetaljer - deltaker ikke 100 prosent deltakelsesmengde og uten dager per uke - lager detalj med prosent`() {
		val deltakerliste = TestData.deltakerliste(
			tiltak = Tiltak("AFT 50%", Tiltakskode.ARBEIDSFORBEREDENDE_TRENING),
		)
		val arrangor = TestData.arrangor(id = deltakerliste.arrangorId)

		val deltaker1 = TestData.deltaker(prosentStilling = 50.0, dagerPerUke = 0.0f, deltakerlisteId = deltakerliste.id)
		val deltaker2 = TestData.deltaker(prosentStilling = 50.0, dagerPerUke = null, deltakerlisteId = deltakerliste.id)

		val detaljer1 = Aktivitetskort.lagDetaljer(deltaker1, deltakerliste, arrangor)
		val detaljer2 = Aktivitetskort.lagDetaljer(deltaker2, deltakerliste, arrangor)

		detaljer1.first { it.label == "Deltakelsesmengde" } shouldBe Detalj("Deltakelsesmengde", "50%")
		detaljer2.first { it.label == "Deltakelsesmengde" } shouldBe Detalj("Deltakelsesmengde", "50%")
	}

	@Test
	fun `lagDetaljer - deltaker uten prosentstilling - lager detalj med dager per uke`() {
		val deltakerliste = TestData.deltakerliste(
			tiltak = Tiltak("AFT noen dager", Tiltakskode.ARBEIDSFORBEREDENDE_TRENING),
		)
		val arrangor = TestData.arrangor(id = deltakerliste.arrangorId)

		val deltaker1 = TestData.deltaker(prosentStilling = 0.0, dagerPerUke = 5f, deltakerlisteId = deltakerliste.id)
		val deltaker2 = TestData.deltaker(prosentStilling = null, dagerPerUke = 1f, deltakerlisteId = deltakerliste.id)

		val detaljer1 = Aktivitetskort.lagDetaljer(deltaker1, deltakerliste, arrangor)
		val detaljer2 = Aktivitetskort.lagDetaljer(deltaker2, deltakerliste, arrangor)

		detaljer1.first { it.label == "Deltakelsesmengde" } shouldBe Detalj("Deltakelsesmengde", "fordelt på 5 dager i uka")
		detaljer2.first { it.label == "Deltakelsesmengde" } shouldBe Detalj("Deltakelsesmengde", "fordelt på 1 dag i uka")
	}

	@Test
	fun `lagDetaljer - deltaker med 0 prosent og 0 dager - lager ikke detalj med deltakelsesmengde`() {
		val deltakerliste = TestData.deltakerliste(
			tiltak = Tiltak("AFT", Tiltakskode.ARBEIDSFORBEREDENDE_TRENING),
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
			tiltak = Tiltak("Oppfølgingstiltak", Tiltakskode.OPPFOLGING),
		)
		val deltaker = TestData.deltaker()
		val arrangor = TestData.arrangor(id = deltakerliste.arrangorId)

		val detaljer = Aktivitetskort.lagDetaljer(deltaker, deltakerliste, arrangor)

		detaljer.find { it.label == "Deltakelsesmengde" } shouldBe null
	}
}
