package jp.co.anyx.model

import jp.co.anyx.constant.PaymentMethod
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("payments")
data class Payment(
    @Id
    val id: Long = 0,
    val price: BigDecimal,
    val priceModifier: BigDecimal,
    val points: Int,
    val paymentMethod: PaymentMethod,
    val dateTime: LocalDateTime,
    val createdAt: LocalDateTime
)
