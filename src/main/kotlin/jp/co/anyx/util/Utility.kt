package jp.co.anyx.util

import jp.co.anyx.model.Payment
import jp.co.anyx.repository.SalesStatement
import jp.co.anyx.request.PaymentRequest
import jp.co.anyx.response.SaleResponse
import java.math.BigDecimal
import java.time.LocalDateTime

fun SalesStatement.toSaleResponse(): SaleResponse {
    return SaleResponse(
        sales = this.price.formatForResponse(),
        points = this.points,
        datetime = this.datetime
    )
}

fun PaymentRequest.toPayment(pointRate: BigDecimal): Payment {
    return try {
        Payment(
            price = this.price.toBigDecimal() * this.priceModifier,
            priceModifier = this.priceModifier,
            paymentMethod = this.paymentMethod,
            points = (this.price.toBigDecimal() * pointRate).toInt(),
            dateTime = this.datetime,
            createdAt = LocalDateTime.now()
        )
    } catch (e: NumberFormatException) {
        throw RuntimeException("wrong value as price")
    }

}

fun BigDecimal.formatForResponse(): String {
    return String.format("%.2f", this)
}