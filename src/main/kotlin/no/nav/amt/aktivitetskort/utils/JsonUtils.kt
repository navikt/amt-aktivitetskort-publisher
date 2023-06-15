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

	fun objectMapper(): ObjectMapper {
		return mapper
	}

	inline fun <reified T> fromJson(jsonStr: String): T {
		return mapper.readValue(jsonStr)
	}
}
