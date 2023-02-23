package jp.co.anyx.config

import com.tailrocks.graphql.datetime.LocalDateTimeScalar
import graphql.schema.idl.RuntimeWiring
import org.flywaydb.core.Flyway
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.graphql.execution.RuntimeWiringConfigurer
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

@Configuration
class AnyXConfig {

    @Bean
    fun runtimeWiringConfigurer(): RuntimeWiringConfigurer {
        val customLocalDate = "CustomLocalDateTime"
        val dateFormat = "yyyy-MM-dd'T'HH:mm:ssX"
        return RuntimeWiringConfigurer { wiringBuilder: RuntimeWiring.Builder ->
            wiringBuilder.scalar(
                LocalDateTimeScalar.create(
                    customLocalDate,
                    false,
                    DateTimeFormatter.ofPattern(dateFormat)
                )
            )
        }
    }

    @Bean(initMethod = "migrate")
    fun flyway(env: Environment): Flyway {
        return Flyway(
            Flyway.configure()
                .dataSource(
                    env.getRequiredProperty("spring.flyway.url"),
                    env.getRequiredProperty("spring.flyway.user"),
                    env.getRequiredProperty("spring.flyway.password")
                )
        )
    }
}

@Component
@ConfigurationProperties(prefix = "payment.rate")
data class PaymentRateConfig(
    val config: Map<String, PaymentConfig>
) {

    data class PaymentConfig(val modifier: ModifierConfig, val points: BigDecimal) {

        data class ModifierConfig(val min: BigDecimal, val max: BigDecimal) {

            init {
                require(max > min) { "max can't be less than min" }
            }
        }
    }
}
