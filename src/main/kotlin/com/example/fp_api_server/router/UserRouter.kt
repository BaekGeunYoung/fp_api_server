package com.example.fp_api_server.router

import com.example.fp_api_server.handler.UserHandler
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.router

class UserRouter(
    private val userHandler: UserHandler
) {
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
