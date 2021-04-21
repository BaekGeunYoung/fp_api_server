package com.example.fp_api_server

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.web.reactive.function.server.RouterFunction

@SpringBootTest
class Test {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun bootTest() {
        val route = (applicationContext.getBean("userRoutes") as RouterFunction<*>)
    }
}
