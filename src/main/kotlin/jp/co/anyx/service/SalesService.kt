package jp.co.anyx.service

import graphql.ErrorType
import jp.co.anyx.config.PaymentRateConfig
import jp.co.anyx.exception.AnyXGraphQLException
import jp.co.anyx.repository.PaymentRepository
import jp.co.anyx.request.PaymentRequest
import jp.co.anyx.request.TimeRangeRequest
import jp.co.anyx.response.PaymentResponse
import jp.co.anyx.response.SalesResponse
import jp.co.anyx.util.formatForResponse
import jp.co.anyx.util.logger
import jp.co.anyx.util.toPayment
import jp.co.anyx.util.toSaleResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Duration

@Service
@EnableCaching
class SalesService(
    private val paymentRepository: PaymentRepository,
    private val paymentRateConfig: PaymentRateConfig,
    @Value("\${anyx.retry.count}") private val retryCount: Long,
    @Value("\${anyx.cache.ttl}") private val cacheTtl: Long
) {

    private val log = logger()

    @Cacheable("sales")
    fun getHourlySalesStatement(timeRange: TimeRangeRequest): Mono<SalesResponse> {
        val salesList = paymentRepository.findHourlySalesStatement(timeRange.startDateTime, timeRange.endDateTime)
            .retry(retryCount)
            .collectList().map {
                it.map { sale -> sale.toSaleResponse() }
            }

        return salesList.map { SalesResponse(sales = it) }
            .cache(Duration.ofSeconds(cacheTtl)) // simple cache to reduce db load, can be improved
            .onErrorResume {
                log.error(
                    "Error retrieving data from db for time rang start: " +
                        "${timeRange.startDateTime} and end: ${timeRange.endDateTime}"
                )
                Mono.error(
                    AnyXGraphQLException(
                        "Undefined error occurred, please try again.",
                        ErrorType.DataFetchingException
                    )
                )
            }
    }

    fun createPayment(paymentRequest: PaymentRequest): Mono<PaymentResponse> {
        val paymentConfig = paymentRequest.getValidPaymentConfig()
        val savedPayment = paymentConfig.flatMap { paymentConfig ->
            paymentRepository.save(paymentRequest.toPayment(pointRate = paymentConfig.points))
                .retry(retryCount)
                .onErrorResume {
                    log.error("Error saving payment request")
                    Mono.error(
                        AnyXGraphQLException(
                            "Undefined error occurred, please try again.",
                            ErrorType.DataFetchingException
                        )
                    )
                }
        }

        return savedPayment.map { payment ->
            PaymentResponse(
                finalPrice = payment.price.formatForResponse(),
                points = payment.points
            )
        }
    }

    private fun PaymentRequest.getValidPaymentConfig(): Mono<PaymentRateConfig.PaymentConfig> {
        // Requested payment method should be present in config file, otherwise invalid.
        val paymentConfig = paymentRateConfig.config[this.paymentMethod.name]
            ?: run {
                val errorMessage = "Invalid payment method in request or invalid config"
                log.error(errorMessage)
                return Mono.error(AnyXGraphQLException(errorMessage, ErrorType.ValidationError))
            }

        // price modifier should be within range of min and max
        if (this.priceModifier !in paymentConfig.modifier.min..paymentConfig.modifier.max) {
            val errorMessage = "priceModifier can not be less than ${paymentConfig.modifier.min} and more " +
                "than ${paymentConfig.modifier.max}"
            log.error(errorMessage)
            return Mono.error(AnyXGraphQLException(errorMessage, ErrorType.ValidationError))
        }

        return paymentConfig.toMono()
    }
}
