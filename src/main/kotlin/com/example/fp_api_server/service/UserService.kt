package com.example.fp_api_server.service

import arrow.Kind
import arrow.core.extensions.list.functor.mapConst
import arrow.core.right
import arrow.fx.typeclasses.Async
import arrow.typeclasses.Monad
import com.example.fp_api_server.entity.User
import com.example.fp_api_server.exception.EntityNotFoundException
import com.example.fp_api_server.repository.UserRepository
import com.example.fp_api_server.typeclass.Suspendable
import com.example.fp_api_server.typeclass.SuspendableMonad
import kotlinx.coroutines.delay

interface UserService<F> {
    fun findById(id: Long): Kind<F, User>
    fun findAll(): Kind<F, List<User>>
    fun delete(id: Long): Kind<F, Unit>
    fun update(user: User): Kind<F, Unit>
    fun insert(user: User): Kind<F, Unit>
}

class UserServiceImpl<F> (
    private val S: Suspendable<F>,
    private val A: Async<F>,
    private val userRepository: UserRepository<F>
) : UserService<F>, Async<F> by A {
    override fun findById(id: Long): Kind<F, User> =
        userRepository.findById(id).map {
            if (it.isPresent) it.get()
            else throw EntityNotFoundException(User::class, id)
        }

    override fun findAll(): Kind<F, List<User>> =
        userRepository.findAll()

    override fun delete(id: Long): Kind<F, Unit> =
        userRepository.findById(id).flatMap {
            userRepository.delete(id)
        }

    override fun update(user: User): Kind<F, Unit> =
        userRepository.findById(user.id).flatMap {
            userRepository.update(user)
        }

    override fun insert(user: User): Kind<F, Unit> =
        userRepository.insert(user)

    fun findAllAndDoSomething(): Kind<F, List<Long>> =
        userRepository.findAll().map { users ->
            users.map {
                async<Long> { callback ->
                    callback(doSomething(it).right())
                }
            }
        }

    val a : (List<Kind<F, Long>>) -> Kind<F, List<Long>> = {

    }

    private fun doSomething(user: User): Long = TODO()
}
