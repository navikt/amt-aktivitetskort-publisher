package no.nav.amt.aktivitetskort

import no.nav.amt.aktivitetskort.database.DbTestDataUtils
import no.nav.amt.aktivitetskort.database.SingletonPostgresContainer
import no.nav.amt.aktivitetskort.mock.servers.MockMachineToMachineHttpServer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest {

	@LocalServerPort
	private var port: Int = 0

	fun serverUrl() = "http://localhost:$port"

	private val client = OkHttpClient.Builder()
		.callTimeout(Duration.ofMinutes(5))
		.build()

	companion object {
		val mockMachineToMachineHttpServer = MockMachineToMachineHttpServer()

		@JvmStatic
		@AfterAll
		fun tearDown() {
			DbTestDataUtils.cleanDatabase(SingletonPostgresContainer.getDataSource())
		}

		@JvmStatic
		@DynamicPropertySource
		fun registerProperties(registry: DynamicPropertyRegistry) {
			SingletonPostgresContainer.getContainer().also {
				registry.add("spring.datasource.url") { it.jdbcUrl }
				registry.add("spring.datasource.username") { it.username }
				registry.add("spring.datasource.password") { it.password }
				registry.add("spring.datasource.hikari.maximum-pool-size") { 3 }
			}

			KafkaContainer(DockerImageName.parse(getKafkaImage())).apply {
				start()
				System.setProperty("KAFKA_BROKERS", bootstrapServers)
			}

/*			mockMachineToMachineHttpServer.start()
			registry.add("nais.env.azureOpenIdConfigTokenEndpoint") {
				mockMachineToMachineHttpServer.serverUrl() + MockMachineToMachineHttpServer.tokenPath
			}
*/

			registry.add("nais.env.azureOpenIdConfigTokenEndpoint") { "" }
			registry.add("amt.arena-acl.url") { "" }

			registry.add("amt.arena-acl.scope") { "test.amt-arena-acl" }
			registry.add("dab.arena-acl.scope") { "test.dab-arena-acl" }
		}

		private fun getKafkaImage(): String {
			val tag = when (System.getProperty("os.arch")) {
				"aarch64" -> "7.2.2-1-ubi8.arm64"
				else -> "7.2.2"
			}

			return "confluentinc/cp-kafka:$tag"
		}
	}

	fun sendRequest(
		method: String,
		path: String,
		body: RequestBody? = null,
		headers: Map<String, String> = emptyMap(),
	): Response {
		val reqBuilder = Request.Builder()
			.url("${serverUrl()}$path")
			.method(method, body)

		headers.forEach {
			reqBuilder.addHeader(it.key, it.value)
		}

		return client.newCall(reqBuilder.build()).execute()
	}
}
