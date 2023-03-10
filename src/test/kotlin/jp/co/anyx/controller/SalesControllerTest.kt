package jp.co.anyx.controller

import com.ninjasquad.springmockk.MockkBean
import graphql.ErrorType
import io.mockk.every
import io.mockk.junit5.MockKExtension
import jp.co.anyx.config.AnyXConfig
import jp.co.anyx.exception.AnyXGraphQLException
import jp.co.anyx.response.PaymentResponse
import jp.co.anyx.response.SaleResponse
import jp.co.anyx.response.SalesResponse
import jp.co.anyx.service.SalesServiceImpl
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.GraphQlTester
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDateTime

@GraphQlTest
@Import(AnyXConfig::class)
@ExtendWith(MockKExtension::class)
class SalesControllerTest {
    @MockkBean
    private lateinit var salesService: SalesServiceImpl

    @MockkBean
    private lateinit var flyway: Flyway

    @Autowired
    private lateinit var tester: GraphQlTester

    private val dateTime = LocalDateTime.now()

    @Test
    fun `should return final price and points successfully`() {
        // mock service method
        every { salesService.createPayment(any()) } returns PaymentResponse(
            finalPrice = "100.00",
            points = 10
        ).toMono()

        val query = """
                mutation{
                  makePayment(payment: {price: "90.50", priceModifier: 0.95, paymentMethod: MASTERCARD, datetime: "2023-02-20T23:07:37" }){
                    finalPrice
                    points
                  }
                }
        """.trimIndent()

        tester
            .document(query)
            .execute()
            .path("makePayment.points")
            .entity(Int::class.java)
            .isEqualTo(10)
            .path("makePayment.finalPrice")
            .entity(String::class.java)
            .isEqualTo("100.00")
    }

    @Test
    fun `should return hourly sales statement successfully`() {
        // mock service method
        every { salesService.getHourlySalesStatement(any()) } returns SalesResponse(
            sales = listOf(
                SaleResponse(
                    sales = "100.00",
                    points = 10,
                    datetime = dateTime.withMinute(0).withSecond(0)
                )
            )
        ).toMono()

        val query = """
                query{
                  getHourlySalesStatement(timeRange:{startDateTime: "2023-02-18T22:07:37Z",endDateTime: "2023-02-24T22:07:37Z"}){
                    sales{
                      datetime
                      sales
                      points
                    }
                  }
                }
        """.trimIndent()

        tester
            .document(query)
            .execute()
            .path("getHourlySalesStatement.sales[0].points")
            .entity(Int::class.java)
            .isEqualTo(10)
            .path("getHourlySalesStatement.sales[0].sales")
            .entity(String::class.java)
            .isEqualTo("100.00")
    }

    @Test
    fun `should return error when request syntax is invalid`() {
        val invalidQuery = """
                query{
                  getHourlySalesStatement(MISSINGCOLON{startDateTime: "2023-02-18T22:07:37Z",endDateTime: "2023-02-24T22:07:37Z"}){
                    sales{
                      datetime
                      sales
                      points
                    }
                  }
                }
        """.trimIndent()

        tester
            .document(invalidQuery)
            .execute()
            .errors()
            .expect { error ->
                error.errorType == ErrorType.InvalidSyntax
            }
            .verify()
    }

    @Test
    fun `should return error when date input is wrong`() {
        val invalidQuery = """
                query{
                  getHourlySalesStatement(timeRange:{startDateTime: "WRONGDATE",endDateTime: "2023-02-24T22:07:37Z"}){
                    sales{
                      datetime
                      sales
                      points
                    }
                  }
                }
        """.trimIndent()

        tester
            .document(invalidQuery)
            .execute()
            .errors()
            .expect { error ->
                error.errorType == ErrorType.ValidationError
            }
            .verify()
    }

    @Test
    fun `should return error when payment method is invalid`() {
        val query = """
                mutation{
                  makePayment(payment: {price: "90.50", priceModifier: 0.95, paymentMethod: WRONG, datetime: "2023-02-20T23:07:37" }){
                    finalPrice
                    points
                  }
                }
        """.trimIndent()

        tester
            .document(query)
            .execute()
            .errors()
            .expect { error ->
                error.errorType == ErrorType.ValidationError
            }
    }

    @Test
    fun `should return error when price modifier is out of range`() {
        // mock service method
        every { salesService.createPayment(any()) } returns Mono.error(
            AnyXGraphQLException(
                "error",
                ErrorType.ValidationError
            )
        )

        val query = """
                mutation{
                  makePayment(payment: {price: "90.50", priceModifier: 0.00, paymentMethod: CASH, datetime: "2023-02-20T23:07:37" }){
                    finalPrice
                    points
                  }
                }
        """.trimIndent()

        tester
            .document(query)
            .execute()
            .errors()
            .expect { error ->
                error.errorType == ErrorType.ValidationError
            }
    }
}
