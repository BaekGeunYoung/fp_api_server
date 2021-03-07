package com.example.fp_api_server.typeclass

import arrow.Kind
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.fix
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.fix
import arrow.typeclasses.Monad

interface SuspendableMonad<F>: Suspendable<F>, Monad<F>

interface Suspendable<F> {
    suspend fun <A> Kind<F, A>.suspended(): A
}

fun IO.Companion.suspendable(): Suspendable<ForIO> = object: Suspendable<ForIO> {
    override suspend fun <A> Kind<ForIO, A>.suspended(): A {
        val io = this.fix()
        return io.suspended()
    }
}

fun MonoK.Companion.suspendable(): Suspendable<ForMonoK> = object: Suspendable<ForMonoK> {
    override suspend fun <A> Kind<ForMonoK, A>.suspended(): A {
        val mono = this.fix()
        return mono.suspended() ?: throw Exception("null")
    }
}
