package no.nav.amt.aktivitetskort.repositories

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.amt.aktivitetskort.domain.Tiltakstype
import no.nav.amt.lib.models.deltakerliste.tiltakstype.Tiltakskode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class TiltakstypeRepositoryTest(
	private val sut: TiltakstypeRepository,
) : RepositoryTestBase() {
	@Nested
	inner class Get {
		@Test
		fun `skal returnere null hvis tiltakstype ikke finnes`() {
			val inDb = sut.getByTiltakskode(tiltakstypeInTest.tiltakskode.name)

			inDb shouldBe null
		}

		@Test
		fun `skal returnere tiltakstype hvis tiltakstype finnes`() {
			sut.upsert(tiltakstypeInTest)

			val inDb = sut.getByTiltakskode(tiltakstypeInTest.tiltakskode.name)

			assertSoftly(inDb.shouldNotBeNull()) {
				id shouldBe tiltakstypeInTest.id
				navn shouldBe tiltakstypeInTest.navn
				tiltakskode shouldBe tiltakstypeInTest.tiltakskode
			}
		}
	}

	companion object {
		private val tiltakstypeInTest = Tiltakstype(
			id = UUID.randomUUID(),
			navn = "~navn~",
			tiltakskode = Tiltakskode.ENKELTPLASS_FAG_OG_YRKESOPPLAERING,
		)
	}
}
