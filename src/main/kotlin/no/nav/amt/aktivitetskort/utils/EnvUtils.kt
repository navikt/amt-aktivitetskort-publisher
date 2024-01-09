package no.nav.amt.aktivitetskort.utils

object EnvUtils {
	fun isDev(): Boolean {
		val cluster = System.getenv("NAIS_CLUSTER_NAME") ?: "Ikke dev"
		return cluster == "dev-gcp"
	}
}
