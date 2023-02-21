package jp.co.anyx

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class AnyXApplication

fun main(args: Array<String>) {
    runApplication<AnyXApplication>(*args)
}
