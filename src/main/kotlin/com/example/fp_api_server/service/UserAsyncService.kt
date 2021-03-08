package com.example.fp_api_server.service

import arrow.fx.IO
import arrow.fx.ForIO
import arrow.fx.IOOf
import arrow.fx.fix
import arrow.Kind
import arrow.core.left
import arrow.core.right
import com.example.fp_api_server.entity.User
import reactor.core.publisher.Mono
import java.time.Duration
import arrow.fx.extensions.io.async.async

interface UserAsyncService {
    fun doSomethingAsync(user: User): IOOf<Long>
}

class MonoUserAsyncService : UserAsyncService {
    override fun doSomethingAsync(user: User): IOOf<Long> =
        IO.async().async<Long> { callback ->
            Mono.delay(Duration.ofMillis(3000L)).map { user.id }.subscribe(
                { result -> callback(result.right()) },
                { e -> callback(e.left()) }
            )
        }.fix()
}
