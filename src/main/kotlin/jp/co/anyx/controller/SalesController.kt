package jp.co.anyx.controller

import jp.co.anyx.request.PaymentRequest
import jp.co.anyx.request.TimeRangeRequest
import jp.co.anyx.response.PaymentResponse
import jp.co.anyx.response.SalesResponse
import jp.co.anyx.service.SalesService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

@Controller
class SalesController(
    private val salesService: SalesService
) {
    @QueryMapping("getHourlySalesStatement")
    fun getHourlySalesStatement(
        @Argument timeRange: TimeRangeRequest
    ): Mono<SalesResponse> {
        return salesService.getHourlySalesStatement(timeRange)
    }

    @MutationMapping("makePayment")
    fun makePayment(@Argument payment: PaymentRequest): Mono<PaymentResponse> {
        return salesService.createPayment(payment)
    }
}
