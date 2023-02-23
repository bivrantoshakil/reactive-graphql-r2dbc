package jp.co.anyx.util

import jp.co.anyx.model.Payment
import jp.co.anyx.repository.SalesStatement
import jp.co.anyx.request.PaymentRequest
import jp.co.anyx.response.PaymentResponse
import jp.co.anyx.response.SaleResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDateTime

fun Any.logger(): Logger = LoggerFactory.getLogger(this.javaClass)

fun SalesStatement.toSaleResponse(): SaleResponse {
    return SaleResponse(
        sales = this.price.formatForResponse(),
        points = this.points,
        datetime = this.datetime
    )
}

fun PaymentRequest.toPayment(pointRate: BigDecimal): Payment {
    return Payment(
        price = this.price.toBigDecimal() * this.priceModifier,
        priceModifier = this.priceModifier,
        paymentMethod = this.paymentMethod,
        points = (this.price.toBigDecimal() * pointRate).toInt(),
        dateTime = this.datetime,
        createdAt = LocalDateTime.now()
    )
}

fun Payment.toPaymentResponse(): PaymentResponse {
    return PaymentResponse(
        finalPrice = this.price.formatForResponse(),
        points = this.points
    )
}

fun BigDecimal.formatForResponse(): String {
    return String.format("%.2f", this)
}
