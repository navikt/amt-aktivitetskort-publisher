package no.nav.amt.aktivitetskort.unleash

import io.getunleash.Unleash
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.aktivitetskort.unleash.UnleashToggle.Companion.ENABLE_KOMET_DELTAKERE
import no.nav.amt.aktivitetskort.unleash.UnleashToggle.Companion.LES_ARENA_DELTAKERE
import no.nav.amt.aktivitetskort.unleash.UnleashToggle.Companion.OPPDATER_ALLE_AKTIVITETSKORT
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class UnleashToggleTest {
	private val unleashClient: Unleash = mockk()
	private val sut = UnleashToggle(unleashClient)

	@BeforeEach
	fun setup() {
		clearAllMocks()
		every { unleashClient.isEnabled(any()) } returns false
	}

	@Nested
	inner class ErKometMasterForTiltakstype {
		@ParameterizedTest
		@EnumSource(
			value = Tiltakskode::class,
			names = [
				"ARBEIDSFORBEREDENDE_TRENING",
				"OPPFOLGING",
				"AVKLARING",
				"ARBEIDSRETTET_REHABILITERING",
				"DIGITALT_OPPFOLGINGSTILTAK",
				"VARIG_TILRETTELAGT_ARBEID_SKJERMET",
				"GRUPPE_ARBEIDSMARKEDSOPPLAERING",
				"JOBBKLUBB",
				"GRUPPE_FAG_OG_YRKESOPPLAERING",
			],
		)
		fun `returnerer true for tiltakstyper som Komet alltid er master for`(kode: Tiltakskode) {
			sut.erKometMasterForTiltakstype(kode.name) shouldBe true
			sut.erKometMasterForTiltakstype(kode) shouldBe true
		}

		@ParameterizedTest
		@EnumSource(
			value = Tiltakskode::class,
			names = [
				"ENKELTPLASS_ARBEIDSMARKEDSOPPLAERING",
				"ENKELTPLASS_FAG_OG_YRKESOPPLAERING",
				"HOYERE_UTDANNING",
			],
		)
		fun `returnerer true hvis toggle ENABLE_KOMET_DELTAKERE er pa for kanskje-master-typer`(kode: Tiltakskode) {
			every { unleashClient.isEnabled(ENABLE_KOMET_DELTAKERE) } returns true

			sut.erKometMasterForTiltakstype(kode) shouldBe true
		}

		@ParameterizedTest
		@EnumSource(
			value = Tiltakskode::class,
			names = [
				"ENKELTPLASS_ARBEIDSMARKEDSOPPLAERING",
				"ENKELTPLASS_FAG_OG_YRKESOPPLAERING",
				"HOYERE_UTDANNING",
			],
		)
		fun `returnerer false hvis toggle ENABLE_KOMET_DELTAKERE er av for kanskje-master-typer`(kode: Tiltakskode) {
			sut.erKometMasterForTiltakstype(kode) shouldBe false
		}
	}

	@Nested
	inner class SkalLeseArenaDataForTiltakstype {
		@ParameterizedTest
		@EnumSource(
			value = Tiltakskode::class,
			names = [
				"ENKELTPLASS_ARBEIDSMARKEDSOPPLAERING",
				"ENKELTPLASS_FAG_OG_YRKESOPPLAERING",
				"HOYERE_UTDANNING",
			],
		)
		fun `returnerer true nar toggle LES_ARENA_DELTAKERE er pa og tiltakstype er lesbar `(kode: Tiltakskode) {
			every { unleashClient.isEnabled(LES_ARENA_DELTAKERE) } returns true

			sut.skalLeseArenaDataForTiltakstype(kode.name) shouldBe true
		}

		@Test
		fun `returnerer false nar toggle LES_ARENA_DELTAKERE er av`() {
			sut.skalLeseArenaDataForTiltakstype(Tiltakskode.ENKELTPLASS_ARBEIDSMARKEDSOPPLAERING.name) shouldBe false
		}

		@Test
		fun `returnerer false for tiltakstyper som ikke er enkeltplass selv om toggle er pa`() {
			every { unleashClient.isEnabled(LES_ARENA_DELTAKERE) } returns true

			sut.skalLeseArenaDataForTiltakstype("ARBEIDSFORBEREDENDE_TRENING") shouldBe false
		}
	}

	@Nested
	inner class SkalOppdatereForUendretDeltaker {
		@Test
		fun `returnerer true nar toggle LES_GJENNOMFORINGER_V2 er pa`() {
			every { unleashClient.isEnabled(OPPDATER_ALLE_AKTIVITETSKORT) } returns true

			sut.skalOppdatereForUendretDeltaker() shouldBe true
		}

		@Test
		fun `returnerer false nar toggle LES_GJENNOMFORINGER_V2 er av`() {
			every { unleashClient.isEnabled(OPPDATER_ALLE_AKTIVITETSKORT) } returns false

			sut.skalOppdatereForUendretDeltaker() shouldBe false
		}
	}

	@Nested
	inner class SkipProsesseringAvGjennomforing {
		@ParameterizedTest
		@EnumSource(
			value = Tiltakskode::class,
			names = [
				"ARBEIDSFORBEREDENDE_TRENING",
				"OPPFOLGING",
				"AVKLARING",
				"ARBEIDSRETTET_REHABILITERING",
				"DIGITALT_OPPFOLGINGSTILTAK",
				"VARIG_TILRETTELAGT_ARBEID_SKJERMET",
				"GRUPPE_ARBEIDSMARKEDSOPPLAERING",
				"JOBBKLUBB",
				"GRUPPE_FAG_OG_YRKESOPPLAERING",
			],
		)
		fun `returnerer false for tiltakskoder Komet er master for `(tiltakskode: Tiltakskode) {
			every { unleashClient.isEnabled(ENABLE_KOMET_DELTAKERE) } returns true
			every { unleashClient.isEnabled(LES_ARENA_DELTAKERE) } returns false

			sut.skipProsesseringAvGjennomforing(tiltakskode.name) shouldBe false
		}

		@ParameterizedTest
		@EnumSource(
			value = Tiltakskode::class,
			names = [
				"ENKELTPLASS_ARBEIDSMARKEDSOPPLAERING",
				"ENKELTPLASS_FAG_OG_YRKESOPPLAERING",
				"HOYERE_UTDANNING",
			],
		)
		fun `returnerer false for enkeltplass tiltakskoder `(tiltakskode: Tiltakskode) {
			every { unleashClient.isEnabled(ENABLE_KOMET_DELTAKERE) } returns false
			every { unleashClient.isEnabled(LES_ARENA_DELTAKERE) } returns true

			sut.skipProsesseringAvGjennomforing(tiltakskode.name) shouldBe false
		}

		@Test
		fun `returnerer true for tiltakskoder som ikke skal prosesseres`() {
			every { unleashClient.isEnabled(ENABLE_KOMET_DELTAKERE) } returns false
			every { unleashClient.isEnabled(LES_ARENA_DELTAKERE) } returns true

			sut.skipProsesseringAvGjennomforing("~tiltakskode~") shouldBe true
		}

		@Test
		fun `returnerer true for tiltakskoder som ikke skal prosesseres #2`() {
			every { unleashClient.isEnabled(ENABLE_KOMET_DELTAKERE) } returns true
			every { unleashClient.isEnabled(LES_ARENA_DELTAKERE) } returns false

			sut.skipProsesseringAvGjennomforing("~tiltakskode~") shouldBe true
		}
	}
}
