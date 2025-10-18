package no.nav.amt.aktivitetskort.utils

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.amt.lib.utils.objectMapper

object JsonUtils {
	// Code review comment: Merk at T kan være null. Benytt <reified T : Any> hvis T ikke skal kunne være null.
	@Deprecated("Bruk objectMapper.readValue<T>() eller objectMapper.readValueOrNull<T>()")
	inline fun <reified T> fromJson(jsonStr: String): T = objectMapper.readValue(jsonStr)

	@Deprecated("Bruk objectMapper.writeValueAsString direkte")
	fun toJsonString(any: Any): String = objectMapper.writeValueAsString(any)
}
