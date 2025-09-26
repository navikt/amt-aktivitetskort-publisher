package no.nav.amt.aktivitetskort.domain

import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.database.TestData
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakstype
import org.junit.jupiter.api.Test

class AktivitetskortTest {
	@Test
	fun `lagTittel - deltakerliste og arrangor - lager riktig tittel basert på type tiltak`() {
		val arrangor = TestData.arrangor()
		val deltakerlister =
			Tiltakstype.Tiltakskode.entries.map {
				val tiltaksnavn = when (it) {
					Tiltakstype.Tiltakskode.ARBEIDSFORBEREDENDE_TRENING -> "Arbforb Tiltak"
					Tiltakstype.Tiltakskode.ARBEIDSRETTET_REHABILITERING -> "ARR"
					Tiltakstype.Tiltakskode.AVKLARING -> "Avklaringstiltaket"
					Tiltakstype.Tiltakskode.DIGITALT_OPPFOLGINGSTILTAK -> "Digitalt jobbsøkerkurs"
					Tiltakstype.Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING -> "Grupper AMO"
					Tiltakstype.Tiltakskode.JOBBKLUBB -> "Jobbklubben"
					Tiltakstype.Tiltakskode.OPPFOLGING -> "Oppfølgingstiltak"
					Tiltakstype.Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET -> "VTA"
					Tiltakstype.Tiltakskode.GRUPPE_FAG_OG_YRKESOPPLAERING -> "Gruppe yrkesfaglig utanning"
				}
				TestData.deltakerliste(tiltak = Tiltak(tiltaksnavn, it), arrangorId = arrangor.id)
			}

		deltakerlister.forEach {
			val aktivitetskortTittel = Aktivitetskort.lagTittel(it, arrangor, true)
			when (it.tiltak.tiltakskode) {
				Tiltakstype.Tiltakskode.DIGITALT_OPPFOLGINGSTILTAK -> aktivitetskortTittel shouldBe "Digitalt jobbsøkerkurs hos ${arrangor.navn}"
				Tiltakstype.Tiltakskode.JOBBKLUBB -> aktivitetskortTittel shouldBe "Jobbsøkerkurs hos ${arrangor.navn}"
				Tiltakstype.Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET -> aktivitetskortTittel shouldBe "Tilrettelagt arbeid hos ${arrangor.navn}"
				Tiltakstype.Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING -> aktivitetskortTittel shouldBe "Kurs: ${it.navn}"
				Tiltakstype.Tiltakskode.GRUPPE_FAG_OG_YRKESOPPLAERING -> aktivitetskortTittel shouldBe it.navn
				else -> aktivitetskortTittel shouldBe "${it.tiltak.navn} hos ${arrangor.navn}"
			}
		}
		deltakerlister.find { it.tiltak.tiltakskode == Tiltakstype.Tiltakskode.GRUPPE_ARBEIDSMARKEDSOPPLAERING }?.let {
			val aktivitetskortTittel = Aktivitetskort.lagTittel(it, arrangor, false)
			aktivitetskortTittel shouldBe it.navn
		}
	}

	@Test
	fun `lagDetaljer - deltakerliste med deltakelsesmengde - lager detaljer i riktig rekkefølge`() {
		val deltakerliste = TestData.deltakerliste(
			tiltak = Tiltak("VTA 100%", Tiltakstype.Tiltakskode.VARIG_TILRETTELAGT_ARBEID_SKJERMET),
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
			tiltak = Tiltak("AFT 50% 2 dager i uken", Tiltakstype.Tiltakskode.ARBEIDSFORBEREDENDE_TRENING),
		)
		val deltaker = TestData.deltaker(prosentStilling = 50.0, dagerPerUke = 2.0f, deltakerlisteId = deltakerliste.id)
		val arrangor = TestData.arrangor(id = deltakerliste.arrangorId)

		val detaljer = Aktivitetskort.lagDetaljer(deltaker, deltakerliste, arrangor)

		detaljer.first { it.label == "Deltakelsesmengde" } shouldBe Detalj("Deltakelsesmengde", "50% fordelt på 2 dager i uka")
	}

	@Test
	fun `lagDetaljer - deltaker ikke 100 prosent deltakelsesmengde og uten dager per uke - lager detalj med prosent`() {
		val deltakerliste = TestData.deltakerliste(
			tiltak = Tiltak("AFT 50%", Tiltakstype.Tiltakskode.ARBEIDSFORBEREDENDE_TRENING),
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
			tiltak = Tiltak("AFT noen dager", Tiltakstype.Tiltakskode.ARBEIDSFORBEREDENDE_TRENING),
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
			tiltak = Tiltak("AFT", Tiltakstype.Tiltakskode.ARBEIDSFORBEREDENDE_TRENING),
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
			tiltak = Tiltak("Oppfølgingstiltak", Tiltakstype.Tiltakskode.OPPFOLGING),
		)
		val deltaker = TestData.deltaker()
		val arrangor = TestData.arrangor(id = deltakerliste.arrangorId)

		val detaljer = Aktivitetskort.lagDetaljer(deltaker, deltakerliste, arrangor)

		detaljer.find { it.label == "Deltakelsesmengde" } shouldBe null
	}
}
