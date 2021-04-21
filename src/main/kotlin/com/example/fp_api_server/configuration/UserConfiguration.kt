package com.example.fp_api_server.configuration

import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.extensions.monok.async.async
import com.example.fp_api_server.handler.UserHandler
import com.example.fp_api_server.repository.UserJpaRepository
import com.example.fp_api_server.repository.UserRepository
import com.example.fp_api_server.repository.UserRepositoryImpl
import com.example.fp_api_server.router.UserRouter
import com.example.fp_api_server.service.UserService
import com.example.fp_api_server.service.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction

@Configuration
class UserConfiguration {
    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    private val monoAsync = MonoK.async()

    @Bean
    fun userRepository(): UserRepository<ForMonoK> =
        UserRepositoryImpl(monoAsync, userJpaRepository)

    @Bean
    fun userService(): UserService<ForMonoK> =
        UserServiceImpl(monoAsync, userRepository())

    @Bean
    fun userHandler(): UserHandler =
        UserHandler(userService())

    @Bean
    fun userRoutes(): RouterFunction<*> =
        UserRouter(userHandler()).userRoutes()
}
