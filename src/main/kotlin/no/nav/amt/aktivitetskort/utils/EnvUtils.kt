package no.nav.amt.aktivitetskort.utils

const val NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME"

object EnvUtils {
	fun isDev(): Boolean {
		val cluster = getEnvVar(NAIS_CLUSTER_NAME) ?: "Ikke dev"
		return cluster == "dev-gcp"
	}
}

fun getEnvVar(varName: String, defaultValue: String? = null) = System.getenv(varName)
	?: System.getProperty(varName)
	?: defaultValue
