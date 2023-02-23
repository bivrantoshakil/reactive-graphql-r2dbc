package jp.co.anyx.service

import io.mockk.every
import io.mockk.mockk
import jp.co.anyx.config.PaymentRateConfig
import jp.co.anyx.constant.PaymentMethod
import jp.co.anyx.exception.AnyXGraphQLException
import jp.co.anyx.repository.PaymentRepository
import jp.co.anyx.repository.SalesStatement
import jp.co.anyx.request.PaymentRequest
import jp.co.anyx.request.TimeRangeRequest
import jp.co.anyx.util.toPayment
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class SalesServiceTest {
    private val paymentRepository = mockk<PaymentRepository>()
    private val paymentConfig = PaymentRateConfig.PaymentConfig(
        points = 0.05.toBigDecimal(),
        modifier = PaymentRateConfig.PaymentConfig.ModifierConfig(
            min = 0.90.toBigDecimal(),
            max = 0.95.toBigDecimal()
        )
    )
    private val paymentRateConfig = PaymentRateConfig(
        config = mapOf(PaymentMethod.JCB.name to paymentConfig)
    )
    private val retryCount: Long = 2
    private val cacheTtl: Long = 300

    private val salesService = SalesServiceImpl(paymentRepository, paymentRateConfig, retryCount, cacheTtl)

    @Test
    fun `should return payment response with valid request`() {
        // test data preparation
        val paymentRequest = PaymentRequest(
            price = "100",
            priceModifier = 0.95.toBigDecimal(),
            paymentMethod = PaymentMethod.JCB,
            datetime = LocalDateTime.now()
        )

        val payment = paymentRequest.toPayment(paymentConfig.points)

        // mock repository
        every { paymentRepository.save(any()) } returns payment.toMono()

        StepVerifier
            .create(salesService.createPayment(paymentRequest))
            .consumeNextWith {
                it.finalPrice shouldBeEqualTo "95.00"
                it.points shouldBeEqualTo 5
            }
            .verifyComplete()
    }

    @Test
    fun `should return error when price modifier is less than min`() {
        // test data preparation
        val paymentRequest = PaymentRequest(
            price = "100",
            priceModifier = 0.85.toBigDecimal(),
            paymentMethod = PaymentMethod.JCB,
            datetime = LocalDateTime.now()
        )

        StepVerifier
            .create(salesService.createPayment(paymentRequest))
            .expectErrorMatches {
                it is AnyXGraphQLException && it.message == "priceModifier can not be less than ${paymentConfig.modifier.min} and more " +
                    "than ${paymentConfig.modifier.max}"
            }
            .verify()
    }

    @Test
    fun `should return error when price modifier is more than max`() {
        // test data preparation
        val paymentRequest = PaymentRequest(
            price = "100",
            priceModifier = 1.toBigDecimal(),
            paymentMethod = PaymentMethod.JCB,
            datetime = LocalDateTime.now()
        )

        StepVerifier
            .create(salesService.createPayment(paymentRequest))
            .expectErrorMatches {
                it is AnyXGraphQLException && it.message == "priceModifier can not be less than ${paymentConfig.modifier.min} and more " +
                    "than ${paymentConfig.modifier.max}"
            }
            .verify()
    }

    @Test
    fun `should return error when payment method configuration not found`() {
        // test data preparation
        val paymentRequest = PaymentRequest(
            price = "100",
            priceModifier = 0.95.toBigDecimal(),
            paymentMethod = PaymentMethod.AMEX,
            datetime = LocalDateTime.now()
        )

        StepVerifier
            .create(salesService.createPayment(paymentRequest))
            .expectErrorMatches {
                it is AnyXGraphQLException && it.message == "Invalid payment method in request or invalid config"
            }
            .verify()
    }

    @Test
    fun `should return error when save db operation fails`() {
        // test data preparation
        val paymentRequest = PaymentRequest(
            price = "100",
            priceModifier = 0.95.toBigDecimal(),
            paymentMethod = PaymentMethod.JCB,
            datetime = LocalDateTime.now()
        )

        every { paymentRepository.save(any()) } returns Mono.error(RuntimeException("DB connection failed"))

        StepVerifier
            .create(salesService.createPayment(paymentRequest))
            .expectErrorMatches {
                it is AnyXGraphQLException && it.message == "Undefined error occurred, please try again."
            }
            .verify()
    }

    @Test
    fun `should return hourly sales sales statement with valid request`() {
        // mock repository
        every {
            paymentRepository.findSalesStatementGroupedByInterval(
                any(),
                any(),
                any()
            )
        } returns SalesStatement(
            price = 100.toBigDecimal(),
            points = 10,
            datetime = LocalDateTime.now()
        ).toMono().toFlux()

        StepVerifier
            .create(
                salesService.getHourlySalesStatement(
                    TimeRangeRequest(
                        LocalDateTime.now().minusMinutes(1),
                        LocalDateTime.now().plusMinutes(1)
                    )
                )
            )
            .consumeNextWith {
                it.sales.size shouldBeEqualTo 1
                it.sales[0].sales shouldBeEqualTo "100.00"
            }
            .verifyComplete()
    }

    @Test
    fun `should return error when read db operation fails`() {
        // mock repository
        every {
            paymentRepository.findSalesStatementGroupedByInterval(
                any(),
                any(),
                any()
            )
        } returns Flux.error(RuntimeException("DB connection failed"))

        StepVerifier
            .create(
                salesService.getHourlySalesStatement(
                    TimeRangeRequest(
                        LocalDateTime.now().minusMinutes(1),
                        LocalDateTime.now().plusMinutes(1)
                    )
                )
            )
            .expectErrorMatches {
                it is AnyXGraphQLException && it.message == "Undefined error occurred, please try again."
            }
            .verify()
    }
}
