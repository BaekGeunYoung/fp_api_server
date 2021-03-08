package com.example.fp_api_server

import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoKOf
import arrow.fx.reactor.fix
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.extensions.monok.async.async
import arrow.fx.reactor.k
import com.example.fp_api_server.repository.impl.MockUserRepository
import com.example.fp_api_server.repository.impl.MonoUserRepository
import com.example.fp_api_server.repository.impl.UserJpaRepository
import com.example.fp_api_server.service.MonoUserAsyncService
import com.example.fp_api_server.service.UserServiceImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.time.Duration
import kotlin.system.measureTimeMillis

class Test {
    @Test
    fun test() {
        runBlocking {
            val elapsed = measureTimeMillis {
                val list = Mono.just(listOf(1, 2, 3, 4, 5)).k().suspended()

                val deferreds = list!!.map { GlobalScope.async { suspendComputation(it) } }
                deferreds.awaitAll()
            }

            println(elapsed)
        }
    }

    private fun asyncComputation(a: Int): Mono<Int> = Mono.delay(Duration.ofMillis(3000L)).map { a }

    private suspend fun suspendComputation(a: Int): Int = delay(3000L).let { a }


    @Test
    fun asyncTest() {
        val monoAsync = MonoK.async()

        val userService = UserServiceImpl(
            monoAsync,
            MockUserRepository(),
            MonoUserAsyncService()
        )

        val time = measureTimeMillis {
            userService.findAllAndDoSomething().fix().mono.block()
        }

        println(time)
    }
}
