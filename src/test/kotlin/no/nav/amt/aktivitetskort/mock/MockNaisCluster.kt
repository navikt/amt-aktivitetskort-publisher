package no.nav.amt.aktivitetskort.mock

import no.nav.amt.aktivitetskort.utils.NAIS_CLUSTER_NAME

fun mockCluster(cluster: String = "dev-gcp", block: () -> Unit) {
	System.setProperty(NAIS_CLUSTER_NAME, cluster)
	block()
	System.clearProperty(NAIS_CLUSTER_NAME)
}
