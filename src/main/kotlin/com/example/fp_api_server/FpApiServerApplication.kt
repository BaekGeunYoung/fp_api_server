package com.example.fp_api_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FpApiServerApplication

fun main(args: Array<String>) {
    runApplication<FpApiServerApplication>(*args)
}
