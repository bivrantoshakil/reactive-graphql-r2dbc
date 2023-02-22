package jp.co.anyx.service

import io.mockk.every
import io.mockk.mockk
import jp.co.anyx.config.PaymentRateConfig
import jp.co.anyx.constant.PaymentMethod
import jp.co.anyx.exception.AnyXGraphQLException
import jp.co.anyx.model.Payment
import jp.co.anyx.repository.PaymentRepository
import jp.co.anyx.request.PaymentRequest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
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

    private val salesService = SalesService(paymentRepository, paymentRateConfig, retryCount, cacheTtl)

    @Test
    fun `should return payment response with valid request`() {
        // test data preparation
        val paymentRequest = PaymentRequest(
            price = "100",
            priceModifier = 0.95.toBigDecimal(),
            paymentMethod = PaymentMethod.JCB,
            datetime = LocalDateTime.now()
        )

        val testPaymentRequest = Payment(
            price = 100.toBigDecimal(),
            priceModifier = 0.95.toBigDecimal(),
            points = 5,
            paymentMethod = PaymentMethod.JCB,
            dateTime = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        // mock repository
        every { paymentRepository.save(any()) } returns testPaymentRequest.toMono()

        StepVerifier
            .create(salesService.createPayment(paymentRequest))
            .consumeNextWith {
                it.finalPrice shouldBeEqualTo "100.00"
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
                it is AnyXGraphQLException && it.message == "Invalid price modifier"
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
                it is AnyXGraphQLException && it.message == "Invalid price modifier"
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
}
