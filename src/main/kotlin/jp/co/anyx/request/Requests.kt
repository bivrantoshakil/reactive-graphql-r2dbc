package jp.co.anyx.request

import jp.co.anyx.constant.PaymentMethod
import java.math.BigDecimal
import java.time.LocalDateTime

data class TimeRangeRequest(
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
)

data class PaymentRequest(
    val price: String,
    val priceModifier: BigDecimal,
    val paymentMethod: PaymentMethod,
    val datetime: LocalDateTime
)