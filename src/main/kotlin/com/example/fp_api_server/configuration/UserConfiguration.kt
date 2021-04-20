package com.example.fp_api_server.configuration

import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.extensions.monok.async.async
import com.example.fp_api_server.handler.UserHandler
import com.example.fp_api_server.repository.UserRepository
import com.example.fp_api_server.repository.impl.MonoUserRepository
import com.example.fp_api_server.repository.impl.UserJpaRepository
import com.example.fp_api_server.service.UserService
import com.example.fp_api_server.service.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserConfiguration {
    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    @Bean
    fun monoUserRepository(): UserRepository<ForMonoK> = MonoUserRepository(userJpaRepository)

    @Bean
    fun userService(): UserService<ForMonoK> =
        UserServiceImpl(
            A = MonoK.async(),
            userRepository = monoUserRepository()
        )

    @Bean
    fun userHandler(): UserHandler =
        UserHandler(userService())
}
