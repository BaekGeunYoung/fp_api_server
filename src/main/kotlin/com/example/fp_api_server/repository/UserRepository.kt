package com.example.fp_api_server.repository

import arrow.Kind
import arrow.fx.typeclasses.Async
import com.example.fp_api_server.entity.User
import kotlinx.coroutines.reactor.asCoroutineDispatcher
import org.springframework.data.jpa.repository.JpaRepository
import reactor.core.scheduler.Schedulers
import java.util.Optional

interface UserRepository<F> {
    fun findById(id: Long): Kind<F, Optional<User>>
    fun findAll(): Kind<F, List<User>>
    fun delete(id: Long): Kind<F, Unit>
    fun update(user: User): Kind<F, Unit>
    fun insert(user: User): Kind<F, Unit>
}

class UserRepositoryImpl<F>(
    private val A: Async<F>,
    private val userJpaRepository: UserJpaRepository
) : UserRepository<F>, Async<F> by A {
    private val ioDispatcher =
        Schedulers.newBoundedElastic(400, 100, "db")
            .asCoroutineDispatcher()

    override fun findById(id: Long): Kind<F, Optional<User>> =
        later(ioDispatcher) { userJpaRepository.findById(id) }

    override fun findAll(): Kind<F, List<User>> =
        later(ioDispatcher) { userJpaRepository.findAll() }

    override fun delete(id: Long): Kind<F, Unit> =
        later(ioDispatcher) { userJpaRepository.deleteById(id) }

    override fun update(user: User): Kind<F, Unit> =
        later(ioDispatcher) { userJpaRepository.save(user) }

    override fun insert(user: User): Kind<F, Unit> =
        later(ioDispatcher) { userJpaRepository.save(user) }
}

interface UserJpaRepository : JpaRepository<User, Long>
