package no.nav.amt.aktivitetskort.domain

import java.time.ZonedDateTime
import java.util.UUID

data class Melding(
	val deltakerId: UUID,
	val deltakerlisteId: UUID,
	val arrangorId: UUID,
	val melding: Aktivitetskort,
	val createdAt: ZonedDateTime = ZonedDateTime.now(),
	val modifiedAt: ZonedDateTime = ZonedDateTime.now()
) {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Melding

		if (deltakerId != other.deltakerId) return false
		if (deltakerlisteId != other.deltakerlisteId) return false
		if (arrangorId != other.arrangorId) return false
		return melding == other.melding
	}

	override fun hashCode(): Int {
		var result = deltakerId.hashCode()
		result = 31 * result + deltakerlisteId.hashCode()
		result = 31 * result + arrangorId.hashCode()
		result = 31 * result + melding.hashCode()
		return result
	}
}
