package no.nav.amt.aktivitetskort.kafka.consumer.dto

import io.kotest.assertions.json.schema.jsonSchema
import io.kotest.assertions.json.schema.obj
import io.kotest.assertions.json.schema.string

object DeltakerlistePayloadJsonSchemas {
	val arrangorSchema = jsonSchema {
		obj {
			withProperty("organisasjonsnummer") { string() }
			additionalProperties = false
		}
	}

	val tiltakstypeSchema = jsonSchema {
		obj {
			withProperty("tiltakskode") { string() }
			additionalProperties = false
		}
	}

	val deltakerlistePayloadV2Schema = jsonSchema {
		obj {
			withProperty("id") { string() }
			withProperty("navn", optional = true) { string() }
			withProperty("tiltakskode", optional = true) { string() }
			withProperty("tiltakstype", optional = true) { tiltakstypeSchema() }
			withProperty("arrangor") { arrangorSchema() }
			additionalProperties = false
		}
	}
}
