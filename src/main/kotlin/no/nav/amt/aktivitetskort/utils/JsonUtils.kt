package no.nav.amt.aktivitetskort.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonUtils {
	val mapper: ObjectMapper = ObjectMapper()
		.registerKotlinModule()
		.registerModule(JavaTimeModule())
		.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	fun objectMapper(): ObjectMapper = mapper

	// Code review comment: Merk at T kan være null. Benytt <reified T : Any> hvis T ikke skal kunne være null.
	inline fun <reified T> fromJson(jsonStr: String): T = mapper.readValue(jsonStr)

	fun toJsonString(any: Any): String = mapper.writeValueAsString(any)
}
