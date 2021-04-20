package com.example.fp_api_server.router

import com.example.fp_api_server.handler.UserHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.router

@Component
class UserRouter(
    private val userHandler: UserHandler
) {
    @Bean
    fun userRoutes(): RouterFunction<*> = router {
        "/users".nest {
            GET("/", userHandler::findAll)
            GET("/{id}", userHandler::findById)
            POST("/", userHandler::insert)
            DELETE("/{id}", userHandler::delete)
            PUT("/{id}", userHandler::update)
        }
    }
}
