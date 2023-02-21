package jp.co.anyx.service

import jp.co.anyx.config.PaymentRateConfig
import jp.co.anyx.repository.PaymentRepository
import jp.co.anyx.request.PaymentRequest
import jp.co.anyx.request.TimeRangeRequest
import jp.co.anyx.response.PaymentResponse
import jp.co.anyx.response.SalesResponse
import jp.co.anyx.util.formatForResponse
import jp.co.anyx.util.toPayment
import jp.co.anyx.util.toSaleResponse
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class SalesService(
    private val paymentRepository: PaymentRepository,
    private val paymentRateConfig: PaymentRateConfig
) {
    @Cacheable("sales")
    fun getHourlySalesStatement(timeRange: TimeRangeRequest): Mono<SalesResponse> {
        // TODO add retry on db failure
        return paymentRepository.findHourlySalesStatement(timeRange.startDateTime, timeRange.endDateTime)
            .collectList().map {
                val sales = it.map { sale ->
                    sale.toSaleResponse()
                }
                SalesResponse(
                    sales = sales
                )
            }
            .cache(Duration.ofMinutes(5)) // simple cache to reduce db call, can be improved
    }

    fun createPayment(payment: PaymentRequest): Mono<PaymentResponse> {
        val paymentConfig =
            paymentRateConfig.config[payment.paymentMethod.name]
                ?: throw RuntimeException("wrong payment method")

        // TODO add retry on db failure
        return paymentRepository.save(payment.toPayment(pointRate = paymentConfig.points))
            .map { savedPayment ->
                PaymentResponse(
                    finalPrice = savedPayment.price.formatForResponse(),
                    points = savedPayment.points
                )
            }
    }
}