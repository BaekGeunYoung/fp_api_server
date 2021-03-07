package com.example.fp_api_server.configuration

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import com.example.fp_api_server.controller.UserController
import com.example.fp_api_server.repository.UserRepository
import com.example.fp_api_server.repository.impl.IOUserRepository
import com.example.fp_api_server.repository.impl.MonoUserRepository
import com.example.fp_api_server.repository.impl.UserJpaRepository
import com.example.fp_api_server.typeclass.suspendable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Configuration
class UserConfiguration {
    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    @Bean
    fun ioUserRepository(): UserRepository<ForIO> = IOUserRepository(userJpaRepository)

    @Bean
    fun monoUserRepository(): UserRepository<ForMonoK> = MonoUserRepository(userJpaRepository)

    @RestController
    @RequestMapping("/users/io")
    class IoUserController(
        ioUserRepository: UserRepository<ForIO>
    ) : UserController<ForIO>(IO.suspendable(), ioUserRepository)

    @RestController
    @RequestMapping("/users/mono")
    class MonoUserController(
        monoUserRepository: UserRepository<ForMonoK>
    ) : UserController<ForMonoK>(MonoK.suspendable(), monoUserRepository)
}
