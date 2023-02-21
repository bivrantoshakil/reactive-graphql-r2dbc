package jp.co.anyx.response

import java.time.LocalDateTime

data class SalesResponse(
    val sales: List<SaleResponse>
)

data class SaleResponse(
    val datetime: LocalDateTime,
    val sales: String,
    val points: Int
)

data class PaymentResponse(
    val finalPrice: String,
    val points: Int
)
