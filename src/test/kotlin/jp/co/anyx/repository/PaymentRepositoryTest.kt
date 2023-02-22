package jp.co.anyx.repository

import jp.co.anyx.constant.PaymentMethod
import jp.co.anyx.model.Payment
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.MountableFile
import reactor.test.StepVerifier
import java.time.Duration
import java.time.LocalDateTime

@Testcontainers
@DataR2dbcTest
@TestPropertySource(properties = ["spring.flyway.enabled=false"])
class PaymentRepositoryTest {

    companion object {
        @Container
        var postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15.2-alpine")
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("init.sql"),
                "/docker-entrypoint-initdb.d/init.sql"
            )

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") {
                (
                    "r2dbc:postgresql://" +
                        postgreSQLContainer.host + ":" + postgreSQLContainer.firstMappedPort + "/" + postgreSQLContainer.databaseName
                    )
            }
            registry.add("spring.r2dbc.username") { postgreSQLContainer.username }
            registry.add("spring.r2dbc.password") { postgreSQLContainer.password }
        }
    }

    @Autowired
    lateinit var template: R2dbcEntityTemplate

    @Autowired
    lateinit var repository: PaymentRepository

    @BeforeEach
    fun setup() {
        template.delete(Payment::class.java).all().block(Duration.ofSeconds(5))
    }

    // test data
    private val testPayment = Payment(
        price = 100.toBigDecimal(),
        priceModifier = 0.95.toBigDecimal(),
        points = 5,
        paymentMethod = PaymentMethod.JCB,
        dateTime = LocalDateTime.now(),
        createdAt = LocalDateTime.now()
    )

    @Test
    fun testDatabaseClientExisted() {
        Assertions.assertNotNull(template)
    }

    @Test
    fun testPaymentRepositoryExisted() {
        Assertions.assertNotNull(repository)
    }

    @Test
    fun `test payment is persisted successfully`() {
        StepVerifier
            .create(repository.save(testPayment))
            .consumeNextWith {
                it shouldBeEqualTo testPayment.copy(id = it.id)
            }
            .verifyComplete()
    }

    @Test
    fun `test hourly sales report is generated successfully`() {
        // prepare test data
        repository.saveAll(listOf(testPayment, testPayment)).blockLast(Duration.ofSeconds(5))

        StepVerifier
            .create(
                repository.findHourlySalesStatement(
                    testPayment.dateTime.minusMinutes(1),
                    testPayment.dateTime.plusMinutes(1)
                )
            )
            .consumeNextWith {
                it.points shouldBeEqualTo testPayment.points * 2
                it.price shouldBeEqualTo testPayment.price * 2.toBigDecimal()
            }
            .verifyComplete()
    }
}
