package com.example.fp_api_server.repository.impl

import arrow.Kind
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoKOf
import arrow.fx.reactor.k
import com.example.fp_api_server.entity.User
import com.example.fp_api_server.repository.UserRepository
import org.springframework.data.jpa.repository.JpaRepository
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.Optional

class MonoUserRepository(
    private val userJpaRepository: UserJpaRepository
) : UserRepository<ForMonoK> {
    private val ioScheduler = Schedulers.newBoundedElastic(400, 100, "db")

    override fun findById(id: Long): MonoKOf<Optional<User>> =
        Mono.fromCallable {
            userJpaRepository.findById(id)
        }.subscribeOn(ioScheduler).k()

    override fun findAll(): MonoKOf<List<User>> =
        Mono.just(
            listOf(
                User(1, "qwe", 20),
                User(2, "qwe", 20),
                User(3, "qwe", 20),
                User(4, "qwe", 20),
                User(5, "qwe", 20)
            )
        ).k()

    override fun delete(id: Long): MonoKOf<Unit> =
        Mono.fromCallable {
            userJpaRepository.deleteById(id)
        }.subscribeOn(ioScheduler).k()

    override fun update(user: User): MonoKOf<Unit> =
        Mono.fromCallable {
            userJpaRepository.save(user)
        }.subscribeOn(ioScheduler).map { Unit }.k()

    override fun insert(user: User): MonoKOf<Unit> =
        Mono.fromCallable {
            userJpaRepository.save(user)
        }.subscribeOn(ioScheduler).map { Unit }.k()
}

class MockUserRepository : UserRepository<ForMonoK> {
    override fun findById(id: Long): Kind<ForMonoK, Optional<User>> =
        Mono.just(Optional.of(User(1, "asd", 30))).k()

    override fun findAll(): Kind<ForMonoK, List<User>> =
        Mono.just(
            listOf(
                User(1, "qwe", 20),
                User(2, "qwe", 20),
                User(3, "qwe", 20),
                User(4, "qwe", 20),
                User(5, "qwe", 20)
            )
        ).k()

    override fun delete(id: Long): Kind<ForMonoK, Unit> = Mono.just(Unit).k()

    override fun update(user: User): Kind<ForMonoK, Unit> = Mono.just(Unit).k()

    override fun insert(user: User): Kind<ForMonoK, Unit> = Mono.just(Unit).k()
}

interface UserJpaRepository: JpaRepository<User, Long>
