package com.example.fp_api_server.handler

import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.fix
import com.example.fp_api_server.entity.User
import com.example.fp_api_server.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono

class UserHandler(
    private val userService: UserService<ForMonoK>
) {
    fun findById(serverRequest: ServerRequest): Mono<ServerResponse> =
        serverRequest.pathVariable("id")
            .toLong()
            .let(userService::findById)
            .fix().mono
            .flatMap {
                status(HttpStatus.OK).body(it, User::class.java)
            }

    fun findAll(serverRequest: ServerRequest): Mono<ServerResponse> =
        userService.findAll()
            .fix().mono
            .flatMap {
                status(HttpStatus.OK).body(it, List::class.java)
            }

    fun delete(serverRequest: ServerRequest): Mono<ServerResponse> =
        serverRequest.pathVariable("id")
            .toLong()
            .let(userService::delete)
            .fix().mono
            .flatMap {
                status(HttpStatus.OK).build()
            }

    fun update(serverRequest: ServerRequest): Mono<ServerResponse> =
        serverRequest.bodyToMono(User::class.java)
            .flatMap {
                userService.update(it).fix().mono
            }
            .flatMap {
                status(HttpStatus.OK).build()
            }

    fun insert(serverRequest: ServerRequest): Mono<ServerResponse> =
        serverRequest.bodyToMono(User::class.java)
            .flatMap {
                userService.insert(it).fix().mono
            }
            .flatMap {
                status(HttpStatus.OK).build()
            }
}