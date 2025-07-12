package no.nav.amt.aktivitetskort

import no.nav.amt.aktivitetskort.database.DbTestDataUtils
import no.nav.amt.aktivitetskort.database.SingletonPostgresContainer
import no.nav.amt.aktivitetskort.database.TestDatabaseService
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import javax.sql.DataSource

@ActiveProfiles("test")
@AutoConfigureJdbc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class RepositoryTestBase {
	@Autowired
	private lateinit var dataSource: DataSource

	@Autowired
	protected lateinit var testDatabase: TestDatabaseService

	@AfterEach
	fun cleanDatabase() {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	companion object {
		@ServiceConnection
		@Suppress("unused")
		private val container = SingletonPostgresContainer.postgresContainer
	}
}
