package jp.co.anyx.repository

import jp.co.anyx.model.Payment
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
interface PaymentRepository : ReactiveCrudRepository<Payment, Long> {

    @Query(
        """
            SELECT SUM(price) as price, SUM(points) as points, DATE_TRUNC('hour', date_time) as datetime 
            FROM payments 
            WHERE date_time >= :startDateTime AND date_time <= :endDateTime
            GROUP BY DATE_TRUNC('hour', date_time);
        """
    )
    fun findHourlySalesStatement(startDateTime: LocalDateTime, endDateTime: LocalDateTime): Flux<SalesStatement>
}

data class SalesStatement(
    val price: BigDecimal,
    val points: Int,
    val datetime: LocalDateTime
)