package jp.co.anyx

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AnyXApplication

fun main(args: Array<String>) {
    runApplication<AnyXApplication>(*args)
}
