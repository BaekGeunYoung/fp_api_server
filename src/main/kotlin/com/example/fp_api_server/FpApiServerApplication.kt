package com.example.fp_api_server

import arrow.Kind
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.fix
import arrow.fx.reactor.k
import arrow.typeclasses.Monad
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Flux

@SpringBootApplication
class FpApiServerApplication

fun main(args: Array<String>) {
    runApplication<FpApiServerApplication>(*args)
}

interface ConcurrentMappable<F>: Monad<F> {
    fun <A, B> Kind<F, List<A>>.concurrentMap(f: (A) -> Kind<F, B>): Kind<F, List<B>>
}

class MonoConcurrentMappable(
    private val M: Monad<ForMonoK>
): ConcurrentMappable<ForMonoK>, Monad<ForMonoK> by M {
    override fun <A, B> Kind<ForMonoK, List<A>>.concurrentMap(f: (A) -> Kind<ForMonoK, B>): Kind<ForMonoK, List<B>> =
        this.fix().mono
            .flatMapMany {
                Flux.fromIterable(it)
            }
            .flatMap {
                f(it).fix().mono
            }
            .collectList()
            .k()
}
