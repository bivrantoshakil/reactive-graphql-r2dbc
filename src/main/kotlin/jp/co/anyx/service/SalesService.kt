package jp.co.anyx.service

import jp.co.anyx.request.PaymentRequest
import jp.co.anyx.request.TimeRangeRequest
import jp.co.anyx.response.PaymentResponse
import jp.co.anyx.response.SalesResponse
import reactor.core.publisher.Mono

interface SalesService {

    fun getHourlySalesStatement(timeRange: TimeRangeRequest): Mono<SalesResponse>

    fun createPayment(paymentRequest: PaymentRequest): Mono<PaymentResponse>
}
